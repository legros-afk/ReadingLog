# ReadingLog

An Android app for a young reader to track daily reading, discover new books, and send a weekly digest to a parent via WhatsApp.

## Features

- Log daily reading sessions with page ranges and impressions
- Browse reading history grouped by date
- Manage a personal book catalogue with favourites and wish list
- Get AI-powered book recommendations (Claude / Anthropic)
- Weekly Friday digest sent to a parent via WhatsApp (OpenClaw)
- Cloud backup via Firebase Firestore, works fully offline

## Prerequisites

- **Android Studio** Ladybug (2024.2) or newer
- **JDK 17+** (bundled with Android Studio)
- **Android SDK** API 26+ (min SDK) and API 35 (target SDK)
- A **Firebase** project (free Spark plan is sufficient)
- An **Anthropic API key** (for book recommendations)
- A **Google Books API key** (optional — basic use works without one, but quota is low)
- An **OpenClaw** instance (for the weekly WhatsApp digest)

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/ReadingLog.git
cd ReadingLog
```

### 2. Firebase setup

1. Go to [console.firebase.google.com](https://console.firebase.google.com) and create a new project.
2. Inside the project, click **Add app** → Android.
   - Package name: `com.flo.readinglog`
   - Register the app and download `google-services.json`.
3. Place `google-services.json` in the `app/` directory.
4. Enable **Authentication**:
   - Authentication → Sign-in method → Google → Enable.
5. Enable **Firestore Database**:
   - Firestore Database → Create database → Start in production mode.
   - Apply the security rules below.

#### Firestore security rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 3. API keys

Copy the example file and fill in your keys:

```bash
cp local.properties.example local.properties
```

Edit `local.properties`:

```properties
sdk.dir=/path/to/your/Android/sdk
GOOGLE_BOOKS_API_KEY=your_google_books_api_key_here
ANTHROPIC_API_KEY=your_anthropic_api_key_here
```

**`GOOGLE_BOOKS_API_KEY`** — Optional but recommended to avoid hitting the anonymous quota.
Get one at [console.cloud.google.com](https://console.cloud.google.com):
- Create or select a project → Enable **Books API** → Credentials → Create API key.

**`ANTHROPIC_API_KEY`** — Required for the Discover / recommendations screen.
Get one at [console.anthropic.com](https://console.anthropic.com) → API Keys → Create key.

> Never commit `local.properties` — it is in `.gitignore`.

### 4. Build and install

Connect a physical device (API 26+) or start an emulator, then:

```bash
./gradlew installDebug
```

Or open the project in Android Studio and press **Run**.

### 5. First launch

1. Sign in with Google when prompted.
2. The app is now ready for use.

### 6. Configure OpenClaw (weekly WhatsApp digest)

The weekly digest is sent every Friday at 18:00 via an [OpenClaw](https://openclaw.io) WhatsApp gateway.

1. In the app, tap the **gear icon** (top-right of the Today screen) to open Settings.
2. Fill in:
   - **OpenClaw Base URL** — your OpenClaw instance URL, e.g. `https://api.openclaw.example.com`
   - **API Token** — your OpenClaw bearer token
   - **Parent WhatsApp Number** — in E.164 format, e.g. `+447700900123`
3. Tap **Save**.
4. Tap **Send test** to verify the connection.

The digest POST shape is:
```
POST {base_url}/send
Authorization: Bearer {token}
Content-Type: application/json

{"to": "+447700900123", "message": "..."}
```
This is intentionally simple — edit `WhatsAppNotifier` if your OpenClaw setup differs.

## Running tests

```bash
./gradlew test
```

Unit tests cover:

- **Page-range validation** (`EntryValidatorTest`)
- **Digest message generation** (`BuildDigestUseCaseTest`)
- **Amazon UK wishlist URL generation** (`BuildDigestUseCaseTest`)
- **Digests flow** using Turbine (`DigestsViewModelTest`)

## Project structure

```
app/src/main/java/com/flo/readinglog/
├── data/
│   ├── local/          Room entities, DAOs, mappers
│   ├── remote/         Retrofit services, WhatsAppNotifier
│   ├── repository/     Repository implementations
│   └── sync/           Firestore sync service
├── di/                 Hilt modules
├── domain/
│   ├── model/          Domain models (Book, ReadingEntry, Digest, …)
│   ├── repository/     Repository interfaces
│   ├── usecase/        BuildDigestUseCase
│   └── validation/     EntryValidator
├── ui/
│   ├── navigation/     Screen sealed class
│   ├── screens/        One package per screen
│   └── theme/          Material 3 theme
└── worker/             SyncWorker, DigestWorker
```

## Architecture

- **MVVM** with single-activity, Compose Navigation
- **Room** as local source of truth; Firestore mirrors it for cloud backup
- **Last-write-wins** conflict resolution (sufficient for single-user use)
- **WorkManager** for periodic Firestore sync (15 min) and Friday digest (weekly)
- API keys loaded from `local.properties` → `BuildConfig` — never in source control
