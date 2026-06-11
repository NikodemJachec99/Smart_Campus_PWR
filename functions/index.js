const { admin, initializeAdminApp } = require("./firebaseAdminInit");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const logger = require("firebase-functions/logger");

initializeAdminApp();

const db = admin.firestore();
const auth = admin.auth();
const messaging = admin.messaging();

const LOGIN_DOMAIN = "smartcampus.local";
const ADMIN_REGION = "europe-west1";

async function sendPushToUser(uid, title, body, data) {
  const userSnap = await db.collection("users").doc(uid).get();
  const tokens =
    userSnap.exists && Array.isArray(userSnap.data().fcmTokens) ? userSnap.data().fcmTokens : [];
  if (tokens.length === 0) {
    return;
  }
  const stringData = {};
  Object.entries(data || {}).forEach(([key, value]) => {
    stringData[key] = String(value);
  });
  const response = await messaging.sendEachForMulticast({
    tokens,
    notification: { title, body },
    data: { title, body, ...stringData },
  });
  const dead = [];
  response.responses.forEach((result, index) => {
    if (
      !result.success &&
      (result.error?.code === "messaging/registration-token-not-registered" ||
        result.error?.code === "messaging/invalid-registration-token")
    ) {
      dead.push(tokens[index]);
    }
  });
  if (dead.length > 0) {
    await db
      .collection("users")
      .doc(uid)
      .update({ fcmTokens: admin.firestore.FieldValue.arrayRemove(...dead) });
  }
}

async function notifyUser(uid, type, title, body, data) {
  await db.collection("notifications").add({
    recipientUid: uid,
    type,
    title,
    body,
    data: data || {},
    read: false,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  await sendPushToUser(uid, title, body, { type, ...(data || {}) });
}

function chatMessagePreview(message) {
  const text = typeof message.text === "string" ? message.text.trim() : "";
  if (text) {
    return text;
  }
  const attachment = message.attachment && typeof message.attachment === "object" ? message.attachment : {};
  const mimeType = typeof attachment.mimeType === "string" ? attachment.mimeType : "";
  if (message.messageType === "image" || mimeType.startsWith("image/")) {
    return "Photo";
  }
  const fileName = typeof attachment.fileName === "string" ? attachment.fileName.trim() : "";
  if (fileName) {
    return `File: ${fileName}`;
  }
  return "Message";
}

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

async function callerHasRole(request, role) {
  if (!request.auth) {
    return false;
  }

  if (request.auth.token?.[role] === true) {
    return true;
  }
  if (role === "tutor" && request.auth.token?.lecturer === true) {
    return true;
  }

  const userDoc = await db.collection("users").doc(request.auth.uid).get();
  if (!userDoc.exists) {
    return false;
  }

  const roles = parseRolesFromUserDoc(userDoc.data());
  return roles.has(role) || (role === "tutor" && roles.has("lecturer"));
}

function requireStringId(value, label) {
  const normalized = typeof value === "string" ? value.trim() : "";
  if (!normalized) {
    throw new HttpsError("invalid-argument", `${label} is required.`);
  }
  return normalized;
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

exports.adminDeleteUser = onCall({ region: ADMIN_REGION }, async (request) => {
  ensureAdminCaller(request);

  const uid = requireStringId(request.data?.uid, "UID");
  if (uid === request.auth.uid) {
    throw new HttpsError("failed-precondition", "Admins cannot delete their own account.");
  }

  const userRef = db.collection("users").doc(uid);
  const userDoc = await userRef.get();
  const existingRoles = parseRolesFromUserDoc(userDoc.exists ? userDoc.data() : {});

  let userRecord = null;
  try {
    userRecord = await auth.getUser(uid);
  } catch (error) {
    if (error?.code !== "auth/user-not-found") {
      throw mapAuthError(error);
    }
  }

  if (existingRoles.has("admin") || userRecord?.customClaims?.admin === true) {
    throw new HttpsError("failed-precondition", "Admin users cannot be deleted from this panel.");
  }

  if (userRecord) {
    await auth.deleteUser(uid);
  }
  await userRef.delete();

  return { uid, deleted: true };
});

exports.deleteAssignment = onCall({ region: ADMIN_REGION }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in before deleting an assignment.");
  }

  const assignmentId = requireStringId(request.data?.assignmentId, "Assignment ID");
  const assignmentRef = db.collection("assignments").doc(assignmentId);
  const assignmentDoc = await assignmentRef.get();
  if (!assignmentDoc.exists) {
    throw new HttpsError("not-found", "Assignment not found.");
  }

  const assignment = assignmentDoc.data() || {};
  const isAdmin = await callerHasRole(request, "admin");
  const isTutorOwner =
    (await callerHasRole(request, "tutor")) && assignment.tutorUid === request.auth.uid;

  if (!isAdmin && !isTutorOwner) {
    throw new HttpsError("permission-denied", "Only the owning tutor or admin can delete this assignment.");
  }

  const submissionsSnapshot = await assignmentRef.collection("submissions").get();
  const bucket = admin.storage().bucket();
  let batch = db.batch();
  let batchWrites = 0;

  async function queueDelete(ref) {
    batch.delete(ref);
    batchWrites += 1;
    if (batchWrites >= 450) {
      await batch.commit();
      batch = db.batch();
      batchWrites = 0;
    }
  }

  for (const submissionDoc of submissionsSnapshot.docs) {
    const storagePath = submissionDoc.get("storagePath");
    if (typeof storagePath === "string" && storagePath.trim()) {
      try {
        await bucket.file(storagePath).delete({ ignoreNotFound: true });
      } catch (error) {
        logger.warn("Failed to delete submission file", { assignmentId, storagePath, error });
      }
    }
    await queueDelete(submissionDoc.ref);
  }

  await queueDelete(assignmentRef);
  if (batchWrites > 0) {
    await batch.commit();
  }

  return {
    assignmentId,
    deleted: true,
    submissionsDeleted: submissionsSnapshot.size,
  };
});

exports.onCourseMessageCreated = onDocumentCreated(
  { region: ADMIN_REGION, document: "courses/{courseId}/messages/{messageId}" },
  async (event) => {
    const message = event.data?.data();
    if (!message) {
      return;
    }
    const courseId = event.params.courseId;
    const messageId = event.params.messageId;
    const courseRef = db.collection("courses").doc(courseId);
    const courseSnap = await courseRef.get();
    const course = courseSnap.data() || {};
    const preview = chatMessagePreview(message);
    await courseRef.update({
      lastMessageText: preview,
      lastMessageSenderName: message.senderName || "",
      lastMessageSenderUid: message.senderUid || "",
      lastMessageIsAnnouncement: message.isAnnouncement === true,
      lastMessageAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    const membersSnap = await db.collection("courses").doc(courseId).collection("members").get();
    const recipients = new Set(membersSnap.docs.map((doc) => doc.id));
    if (course.tutorUid) {
      recipients.add(course.tutorUid);
    }
    recipients.delete(message.senderUid);

    const isAnnouncement = message.isAnnouncement === true;
    const title = isAnnouncement
      ? `Announcement: ${course.name || "Course"}`
      : `${course.name || "Course"}: ${message.senderName}`;
    const body = preview;

    await Promise.all(
      [...recipients].map((uid) =>
        notifyUser(uid, isAnnouncement ? "announcement" : "chat", title, body, { courseId, messageId })
      )
    );
  }
);

exports.onDirectMessageCreated = onDocumentCreated(
  { region: ADMIN_REGION, document: "conversations/{conversationId}/messages/{messageId}" },
  async (event) => {
    const message = event.data?.data();
    if (!message) {
      return;
    }
    const conversationId = event.params.conversationId;
    const messageId = event.params.messageId;
    const conversationRef = db.collection("conversations").doc(conversationId);
    const conversation = (await conversationRef.get()).data() || {};
    const preview = chatMessagePreview(message);

    await conversationRef.update({
      lastMessageText: preview,
      lastMessageSenderName: message.senderName || "",
      lastMessageSenderUid: message.senderUid || "",
      lastMessageAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    const participants = Array.isArray(conversation.participants) ? conversation.participants : [];
    await Promise.all(
      participants
        .filter((uid) => uid !== message.senderUid)
        .map((uid) =>
          notifyUser(uid, "chat", `Message from ${message.senderName}`, preview, {
            conversationId,
            messageId,
          })
        )
    );
  }
);

exports.onAssignmentCreated = onDocumentCreated(
  { region: ADMIN_REGION, document: "assignments/{assignmentId}" },
  async (event) => {
    const assignment = event.data?.data();
    if (!assignment || !assignment.courseId) {
      return;
    }
    const membersSnap = await db
      .collection("courses")
      .doc(assignment.courseId)
      .collection("members")
      .get();
    const title = `New assignment: ${assignment.title || ""}`;
    const body = assignment.courseName
      ? `${assignment.courseName} — due ${assignment.dueDate || "soon"}`
      : assignment.dueDate || "";

    await Promise.all(
      membersSnap.docs.map((doc) =>
        notifyUser(doc.id, "assignment", title, body, {
          assignmentId: event.params.assignmentId,
          courseId: assignment.courseId,
        })
      )
    );
  }
);

exports.dailyDeadlineReminder = onSchedule(
  { region: ADMIN_REGION, schedule: "0 8 * * *", timeZone: "Europe/Warsaw" },
  async () => {
    const now = admin.firestore.Timestamp.now();
    const in24h = admin.firestore.Timestamp.fromMillis(now.toMillis() + 24 * 60 * 60 * 1000);
    const dueSnap = await db
      .collection("assignments")
      .where("dueAt", ">", now)
      .where("dueAt", "<=", in24h)
      .get();

    for (const assignmentDoc of dueSnap.docs) {
      const assignment = assignmentDoc.data();
      if (!assignment.courseId) {
        continue;
      }
      const [membersSnap, submissionsSnap] = await Promise.all([
        db.collection("courses").doc(assignment.courseId).collection("members").get(),
        assignmentDoc.ref.collection("submissions").get(),
      ]);
      const submitted = new Set(submissionsSnap.docs.map((doc) => doc.id));
      await Promise.all(
        membersSnap.docs
          .filter((member) => !submitted.has(member.id))
          .map((member) =>
            notifyUser(
              member.id,
              "deadline",
              `Deadline soon: ${assignment.title || ""}`,
              `${assignment.courseName || "Course"} — due ${assignment.dueDate || ""}`,
              { assignmentId: assignmentDoc.id, courseId: assignment.courseId }
            )
          )
      );
    }
  }
);
