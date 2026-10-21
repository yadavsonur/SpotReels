# SpotReels 🎬

**SpotReels** is an Android application for sharing and discovering short-form videos. Users can create an account, browse a vertical reels feed, upload videos with captions, like and comment on reels, share video links, and view their own uploads.

The app is built with **Java**, Android Studio, **Firebase Authentication**, **Cloud Firestore**, **Supabase Storage**, and **AndroidX Media3 (ExoPlayer)**.

## Features

- **Authentication** — sign-in and sign-up powered by Firebase Authentication.
- **Vertical video feed** — browse reels in a swipe-style feed using RecyclerView and paging snap behavior.
- **Video playback** — play videos using AndroidX Media3 / ExoPlayer.
- **Video uploads** — select a video from the device, add a caption, and upload the file to Supabase Storage.
- **Cloud metadata** — store reel captions, video URLs, uploader IDs, timestamps, and like data in Cloud Firestore.
- **Likes** — toggle a like on a reel, with the signed-in user's ID tracked in the reel data.
- **Comments** — open a dedicated comments screen for a reel.
- **Share** — share a reel's video URL through Android's share sheet.
- **My uploads** — view videos uploaded by the currently signed-in user.

## Tech stack

| Component | Technology |
| --- | --- |
| Language | Java |
| Android UI | Android SDK, AndroidX, Material Components |
| Lists and feed | RecyclerView, LinearLayoutManager, PagerSnapHelper |
| Video playback | AndroidX Media3 / ExoPlayer |
| Authentication | Firebase Authentication |
| Metadata and social interactions | Cloud Firestore |
| Video storage | Supabase Storage |
| HTTP uploads | OkHttp |
| Thumbnail / image loading | Glide |
| Build system | Gradle Kotlin DSL |

## How it works

SpotReels keeps video files and app metadata in separate services:

1. A user signs in through Firebase Authentication.
2. The user selects a video and adds a caption.
3. The Android app uploads the video file to a Supabase Storage bucket.
4. Once the upload succeeds, the app saves a Firestore document in the `reels` collection with the reel ID, video URL, caption, uploader ID, timestamp, and like data.
5. The main feed listens to Firestore for reels ordered by timestamp and displays them in a vertical RecyclerView.
6. Media3 / ExoPlayer streams the selected reel from its video URL. Likes and comments use Firestore-backed data.

> **Storage and security note:** the current implementation constructs public object URLs for the `videos` bucket. Review the bucket's visibility and Supabase Storage policies before using real user content. Client-side app keys are not a substitute for correctly configured backend access policies.

## Getting started

### Prerequisites

- Android Studio
- JDK compatible with your installed Android Gradle Plugin
- Android SDK Platform 34
- A Firebase project with **Authentication** and **Cloud Firestore** enabled
- A Supabase project with a Storage bucket configured for video uploads

### 1. Clone the repository

```bash
git clone https://github.com/yadavsonur/SpotReels.git
cd SpotReels
```

Open the project folder in Android Studio and allow Gradle to sync.

### 2. Configure Firebase

1. Create or choose a Firebase project.
2. Register an Android app with the package name `com.sonu.spotreels`.
3. Enable the sign-in methods you intend to support in Firebase Authentication.
4. Create a Cloud Firestore database and configure its security rules.
5. Add your Firebase Android configuration file at `app/google-services.json`.

Do not commit Firebase service-account credentials or other private administrative credentials. Firebase client configuration is not a replacement for appropriate Firestore security rules.

### 3. Configure Supabase

Create a Storage bucket named `videos` or update the bucket name in the upload code to match your setup. Configure Storage policies for the upload and access patterns you intend to allow.

The app reads the Supabase key from a Gradle project property named `SUPABASE_KEY`, which is used to generate the `supabase_key` string resource.

Configure the property locally rather than hard-coding credentials. For example, add the following to your user-level Gradle properties file (commonly `~/.gradle/gradle.properties`):

```properties
SUPABASE_KEY=your_supabase_key
```

Use a key intended for client-side use and enforce access through Supabase Row Level Security / Storage policies. **Never place a Supabase service-role key in an Android application.**

### 4. Build and run

- Select an Android emulator or connect a compatible Android device.
- Sync the project with Gradle.
- Build and run the `app` configuration from Android Studio.

The app's current Gradle configuration uses `minSdk 24`, `targetSdk 34`, and `compileSdk 34`.

## Project structure

```text
SpotReels/
├── app/
│   ├── src/main/
│   │   ├── java/com/sonu/spotreels/
│   │   │   ├── adapters/       # Reel and comment RecyclerView adapters
│   │   │   ├── manager/        # Like operations
│   │   │   ├── model/          # Reel and Comment data models
│   │   │   ├── player/         # Shared Media3 player manager
│   │   │   └── ui/
│   │   │       ├── auth/       # Sign-in and sign-up
│   │   │       ├── comments/   # Comments screen
│   │   │       ├── main/       # Main reels feed
│   │   │       ├── profile/    # Current user's uploads
│   │   │       └── upload/     # Supabase video upload flow
│   │   ├── res/layout/         # Activity and item layouts
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

## Data model overview

A reel document in Firestore's `reels` collection contains fields similar to:

```json
{
  "id": "unique-reel-id",
  "videoUrl": "https://your-project.supabase.co/storage/v1/object/public/videos/uploads/unique-reel-id.mp4",
  "caption": "A short caption",
  "uploadedBy": "firebase-auth-user-id",
  "timestamp": 1720000000000,
  "likes": 0,
  "likedBy": []
}
```

The URL and values above are illustrative examples. The exact document shape should remain consistent with the app's model classes and Firestore operations.

## Important security considerations

- Configure Firestore rules so users cannot impersonate another uploader or arbitrarily modify another user's reel and likes.
- Configure Supabase Storage policies to restrict who may upload, overwrite, or delete objects.
- Do not embed Supabase service-role credentials or private administrative keys in the APK.
- Treat a public video URL as accessible to anyone who obtains it; use private storage and authenticated access if videos should not be public.
- Test upload failures, large files, interrupted network connections, sign-out behavior, and playback errors on real devices.

## Testing

The repository includes basic Android unit-test and instrumentation-test scaffolding. Run tests from Android Studio or use the Gradle wrapper:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

On Windows, use:

```powershell
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest
```

Instrumentation tests require a connected Android device or configured emulator. Cloud-backed flows also require valid Firebase and Supabase configuration.

## Future improvements

Potential next steps include stronger server-side validation for uploads and likes, private-bucket support, improved user profiles, pagination for large feeds, better upload progress/retry handling, and expanded automated UI tests.

## Author

**Sonu Yadav**  
GitHub: [@yadavsonur](https://github.com/yadavsonur)

---

SpotReels is a learning and development project. Configure and verify all cloud security rules before using it with real users or sensitive content.
