const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");

function isLikelyGoogleServicesJson(payload) {
  return !!(payload && payload.project_info && payload.client && Array.isArray(payload.client));
}

function loadServiceAccountFromFile(filePath) {
  const absolutePath = path.isAbsolute(filePath)
    ? filePath
    : path.resolve(process.cwd(), filePath);

  if (!fs.existsSync(absolutePath)) {
    throw new Error(`Service account file not found: ${absolutePath}`);
  }

  const raw = fs.readFileSync(absolutePath, "utf8");
  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch {
    throw new Error(`Invalid JSON in service account file: ${absolutePath}`);
  }

  if (isLikelyGoogleServicesJson(parsed)) {
    throw new Error(
      "Provided file looks like google-services.json (Android client config). " +
        "Use Firebase Service Account key JSON instead."
    );
  }

  if (!parsed.client_email || !parsed.private_key) {
    throw new Error(
      "Invalid service account file: expected fields client_email and private_key."
    );
  }

  return parsed;
}

function initializeAdminApp() {
  if (admin.apps.length > 0) {
    return admin.app();
  }

  const explicitPath =
    process.env.FIREBASE_SERVICE_ACCOUNT_PATH || process.env.GOOGLE_APPLICATION_CREDENTIALS;

  if (explicitPath) {
    const serviceAccount = loadServiceAccountFromFile(explicitPath);
    return admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
      projectId: serviceAccount.project_id || process.env.GOOGLE_CLOUD_PROJECT,
    });
  }

  return admin.initializeApp();
}

module.exports = {
  admin,
  initializeAdminApp,
};
