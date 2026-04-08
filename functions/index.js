const { admin, initializeAdminApp } = require("./firebaseAdminInit");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

initializeAdminApp();

const db = admin.firestore();
const auth = admin.auth();

const LOGIN_DOMAIN = "smartcampus.local";
const ADMIN_REGION = "europe-west1";

function ensureAdminCaller(request) {
  if (!request.auth || request.auth.token.admin !== true) {
    throw new HttpsError("permission-denied", "Only admin can call this function.");
  }
}

function normalizeLogin(login) {
  if (typeof login !== "string") {
    throw new HttpsError("invalid-argument", "Login must be a string.");
  }

  const normalized = login.trim().toLowerCase();
  if (!normalized) {
    throw new HttpsError("invalid-argument", "Login cannot be empty.");
  }

  if (!/^[a-z0-9._-]+$/.test(normalized)) {
    throw new HttpsError(
      "invalid-argument",
      "Login can only contain lowercase letters, numbers, dot, underscore, and dash."
    );
  }

  return normalized;
}

function toRoleArray(rolePayload, { allowAdmin = false } = {}) {
  if (!rolePayload || typeof rolePayload !== "object") {
    throw new HttpsError("invalid-argument", "Roles payload is required.");
  }

  const adminRole = allowAdmin && rolePayload.admin === true;
  const student = rolePayload.student === true;
  const tutor = rolePayload.tutor === true || rolePayload.lecturer === true;

  if (!adminRole && !student && !tutor) {
    throw new HttpsError("invalid-argument", "At least one role must be selected.");
  }

  const roles = [];
  if (adminRole) {
    roles.push("admin");
  }
  if (student) {
    roles.push("student");
  }
  if (tutor) {
    roles.push("tutor");
  }

  return { roles, adminRole, student, tutor };
}

function parseRolesFromUserDoc(data) {
  const result = new Set();

  if (Array.isArray(data?.roles)) {
    for (const role of data.roles) {
      if (typeof role !== "string") {
        continue;
      }
      const normalized = role.trim().toLowerCase();
      if (normalized === "lecturer") {
        result.add("tutor");
      } else if (normalized) {
        result.add(normalized);
      }
    }
  }

  if (result.size === 0 && typeof data?.role === "string") {
    const legacyRole = data.role.trim().toLowerCase();
    if (legacyRole === "lecturer") {
      result.add("tutor");
    } else if (legacyRole) {
      result.add(legacyRole);
    }
  }

  return result;
}

function mapAuthError(error) {
  if (error instanceof HttpsError) {
    return error;
  }

  const code = error?.code || "";
  if (code === "auth/email-already-exists") {
    return new HttpsError("already-exists", "User with this login already exists.");
  }
  if (code === "auth/invalid-password") {
    return new HttpsError("invalid-argument", "Invalid password.");
  }
  if (code === "auth/uid-already-exists") {
    return new HttpsError("already-exists", "UID already exists.");
  }
  if (code === "auth/user-not-found") {
    return new HttpsError("not-found", "User not found.");
  }

  logger.error("Unhandled backend error", error);
  return new HttpsError("internal", "Internal server error.");
}

async function ensureUniqueLogin(loginLowercase) {
  const snapshot = await db
    .collection("users")
    .where("loginLowercase", "==", loginLowercase)
    .limit(1)
    .get();

  if (!snapshot.empty) {
    throw new HttpsError("already-exists", "User with this login already exists.");
  }
}

async function ensureUniqueEmail(email) {
  try {
    await auth.getUserByEmail(email);
    throw new HttpsError("already-exists", "User with this login already exists.");
  } catch (error) {
    if (error instanceof HttpsError) {
      throw error;
    }
    if (error?.code !== "auth/user-not-found") {
      throw error;
    }
  }
}

exports.adminCreateUser = onCall({ region: ADMIN_REGION }, async (request) => {
  ensureAdminCaller(request);

  const data = request.data || {};
  const loginLowercase = normalizeLogin(data.login);
  const password = typeof data.password === "string" ? data.password : "";
  const displayName = typeof data.displayName === "string" ? data.displayName.trim() : "";
  const creatorUid = request.auth.uid;

  if (password.length < 6) {
    throw new HttpsError("invalid-argument", "Password must be at least 6 characters long.");
  }

  const { roles, adminRole, student, tutor } = toRoleArray(data.roles, { allowAdmin: true });
  const email = `${loginLowercase}@${LOGIN_DOMAIN}`;

  await ensureUniqueLogin(loginLowercase);
  await ensureUniqueEmail(email);

  let createdUserRecord;
  try {
    createdUserRecord = await auth.createUser({
      email,
      password,
      displayName: displayName || loginLowercase,
    });

    await auth.setCustomUserClaims(createdUserRecord.uid, {
      admin: adminRole,
      student,
      tutor,
      lecturer: tutor,
    });

    await db.collection("users").doc(createdUserRecord.uid).set({
      login: loginLowercase,
      loginLowercase,
      email,
      displayName: displayName || loginLowercase,
      roles,
      isActive: true,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      createdBy: creatorUid,
    });

    return {
      uid: createdUserRecord.uid,
      email,
      roles,
    };
  } catch (error) {
    if (createdUserRecord) {
      try {
        await auth.deleteUser(createdUserRecord.uid);
      } catch (rollbackError) {
        logger.error("Rollback failed for created user", rollbackError);
      }
    }

    throw mapAuthError(error);
  }
});

exports.adminUpdateUserRoles = onCall({ region: ADMIN_REGION }, async (request) => {
  ensureAdminCaller(request);

  const data = request.data || {};
  const uid = typeof data.uid === "string" ? data.uid.trim() : "";
  if (!uid) {
    throw new HttpsError("invalid-argument", "UID is required.");
  }

  const { roles, student, tutor } = toRoleArray(data.roles);

  let userRecord;
  try {
    userRecord = await auth.getUser(uid);
  } catch (error) {
    throw mapAuthError(error);
  }

  const userRef = db.collection("users").doc(uid);
  const currentUserDoc = await userRef.get();
  const oldData = currentUserDoc.exists ? currentUserDoc.data() : {};
  const existingRoles = parseRolesFromUserDoc(oldData);

  if (existingRoles.has("admin") || userRecord.customClaims?.admin === true) {
    throw new HttpsError("failed-precondition", "Admin user roles cannot be edited from this panel.");
  }

  const oldStudent = userRecord.customClaims?.student === true;
  const oldTutor = userRecord.customClaims?.tutor === true || userRecord.customClaims?.lecturer === true;

  await userRef.set(
    {
      roles,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  try {
    await auth.setCustomUserClaims(uid, {
      admin: false,
      student,
      tutor,
      lecturer: tutor,
    });
  } catch (error) {
    await userRef.set(
      {
        roles:
          oldStudent || oldTutor
            ? [oldStudent ? "student" : null, oldTutor ? "tutor" : null].filter(Boolean)
            : [],
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    throw mapAuthError(error);
  }

  return {
    uid,
    roles,
  };
});