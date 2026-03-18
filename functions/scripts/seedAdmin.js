const { admin, initializeAdminApp } = require("../firebaseAdminInit");

initializeAdminApp();

const db = admin.firestore();
const auth = admin.auth();

const ADMIN_EMAIL = "admin@smartcampus.local";
const ADMIN_PASSWORD = "admin123";
const ADMIN_LOGIN = "admin";

async function ensureAdminUser() {
  let userRecord;

  try {
    userRecord = await auth.getUserByEmail(ADMIN_EMAIL);
    await auth.updateUser(userRecord.uid, {
      password: ADMIN_PASSWORD,
      displayName: "Administrator",
    });
  } catch (error) {
    if (error.code !== "auth/user-not-found") {
      throw error;
    }

    userRecord = await auth.createUser({
      email: ADMIN_EMAIL,
      password: ADMIN_PASSWORD,
      displayName: "Administrator",
    });
  }

  await auth.setCustomUserClaims(userRecord.uid, {
    admin: true,
    student: false,
    lecturer: false,
  });

  const userRef = db.collection("users").doc(userRecord.uid);
  const existingDoc = await userRef.get();
  const createdAt = existingDoc.exists
    ? existingDoc.get("createdAt") || admin.firestore.FieldValue.serverTimestamp()
    : admin.firestore.FieldValue.serverTimestamp();

  await userRef.set(
    {
      login: ADMIN_LOGIN,
      loginLowercase: ADMIN_LOGIN,
      email: ADMIN_EMAIL,
      displayName: "Administrator",
      roles: ["admin"],
      isActive: true,
      createdBy: "seed-script",
      createdAt,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  console.log(`Admin user ready: ${userRecord.uid} (${ADMIN_EMAIL})`);
}

ensureAdminUser()
  .then(() => {
    console.log("Seed completed successfully.");
    process.exit(0);
  })
  .catch((error) => {
    console.error("Seed failed:", error.message || error);
    process.exit(1);
  });
