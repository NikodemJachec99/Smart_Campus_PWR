# Firebase Functions (Admin User Management)

Callable functions:
- `adminCreateUser`
- `adminUpdateUserRoles`

Seed script:
- `npm run seed:admin`

## Required credentials (local)

Local scripts require Firebase Admin credentials.

PowerShell example:

```powershell
$env:FIREBASE_SERVICE_ACCOUNT_PATH="C:\\secure\\serviceAccountKey.json"
$env:GOOGLE_CLOUD_PROJECT="smartcampuspwr-91d9f"
npm run seed:admin
```

Alternative variable supported:
- `GOOGLE_APPLICATION_CREDENTIALS`

## Important

`app/google-services.json` is an Android client config file.
It is **not** a Firebase Admin service account key and cannot be used by `firebase-admin`.

Use a JSON key generated in Firebase Console:
Project settings -> Service accounts -> Generate new private key.
