# Smart Campus PWR

Aplikacja Android (Kotlin + Jetpack Compose) do zarzadzania kampusem uczelnianym.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Firebase Auth / Firestore / Storage / Functions
- Navigation Compose, Coil

## Setup

1. Otworz projekt w Android Studio
2. SDK z API `36`
3. Sync Gradle, uruchom na emulatorze

Backend:
```
cd functions
npm install
```

## Konto testowe

- login: `admin`
- email: `admin@smartcampus.local`
- haslo: `admin123`

## Firebase

- Project ID: `smartcampuspwr-91d9f`
- Package: `Smart.Campus.PWR`
- Config: `app/google-services.json`

Reguly Firestore: `firestore.rules`
Reguly Storage: `storage.rules`

## Build

```
.\gradlew.bat assembleDebug
```
