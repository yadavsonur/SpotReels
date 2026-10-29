# SpotReels data model v1

## Goals and boundaries

This is the proposed domain/persistence contract for the next version of SpotReels. It expands the current reels, likes, comments, authentication and uploads into a maintainable short-video social application. It is a design baseline, not a claim that every listed feature is implemented.

The first scope includes:
- User profiles and account lifecycle state
- Reels and media metadata
- Comments
- Likes
- Follow relationships
- Bookmarks/saved reels
- Notifications
- User-generated-content reports and moderation state

Direct messages, stories, livestreams, ads, creator monetization, and analytics event pipelines are deliberately out of v1 scope. Add them only when product requirements are agreed.

## Source of truth

- Firebase Authentication owns identity and authentication credentials. Do not copy passwords or tokens into profile documents or DataStore.
- Firestore owns profile metadata, reel metadata, comments, social relationships, counters/notification records and moderation records.
- Supabase Storage owns the media bytes. Store bucket + object path as the durable media reference; derive a playback URL through a storage/access service rather than assuming a public URL never changes.
- DataStore owns device-local preferences only (theme, autoplay, onboarding completion, preferred playback quality).
- Client-side models and validation improve correctness but do not enforce security. Firestore Security Rules, Supabase Storage policies and, where needed, trusted server functions must enforce authorization and counter integrity.

## Domain entities

### UserProfile
Key: Firebase Auth UID. Fields: handle (normalized and unique through a trusted uniqueness strategy), display name, bio, avatar storage path, account visibility, account status, created/updated timestamps, optional follower/following/reel counts. Do not expose email as public profile data by default. Counts are denormalized display hints, not authorization data.

### Reel
Key: immutable reel ID. Fields: owner UID, caption, media metadata, visibility, publication/moderation status, created/updated/published timestamps, optional hashtag tokens, and denormalized counts (likes, comments, views). Media metadata contains storage path, bucket, MIME type, byte size, duration, dimensions, and optional thumbnail path. Store the media path rather than only a public URL. A view count strategy must be defined before counting every playback as a view.

### Comment
Key: comment ID under a reel. Fields: reel ID, author UID, text, created/updated timestamps, optional parent comment ID for future replies, and moderation/deletion state. The v1 UI can disable replies while the schema reserves a parent reference. Preserve author UID for authorization; display name/avatar can be joined from the profile or denormalized as a snapshot if product requirements justify it.

### Like
A relationship, not an array on the Reel document. Key: user UID under a reel's likes subcollection. This prevents an ever-growing `likedBy` array from being embedded in a single document. Reel like count is denormalized and must be maintained atomically by a trusted transaction/function or a rigorously secured server-side mechanism.

### Follow
A directional relationship from follower UID to followed UID. Persist canonical relationship records and choose one canonical path for writes; optional reverse indexes may be maintained for efficient follower queries. Prevent self-follow and duplicate relationships. Private-account follow requests are a future extension if private profiles ship in v1.

### Bookmark
A relationship between a user and a reel, keyed by reel ID in the user's saved collection. It is private to the user and should not be visible through public profile reads by default.

### Notification
Key: notification ID under a recipient. Fields: recipient UID, actor UID (optional for system events), type, target entity IDs, creation/read timestamps, and a small presentation snapshot if needed. Notifications are derived records; avoid storing sensitive content or trusting client-supplied actor IDs.

### ContentReport
Key: report ID. Fields: reporter UID, target type and ID, reason, optional details, creation time, review status, and moderation metadata. Access should be tightly restricted; normal users must not read arbitrary reports or change moderation status.

## Suggested Firestore layout

```text
users/{uid}
users/{uid}/following/{targetUid}
users/{uid}/followers/{followerUid}           # optional reverse index
users/{uid}/bookmarks/{reelId}
users/{uid}/notifications/{notificationId}

reels/{reelId}
reels/{reelId}/likes/{uid}
reels/{reelId}/comments/{commentId}

reports/{reportId}
handles/{normalizedHandle}                     # only if using a trusted uniqueness reservation
```

Use the user document ID as the Auth UID. Use server timestamps for persisted created/updated times. In domain/UI models, timestamps can be represented as epoch milliseconds initially to remain compatible with the existing Java code; map Firestore Timestamp/server timestamp values at the repository boundary.

## Important invariants

1. IDs are stable and never derived from display names or list positions.
2. Auth UID is the authority for the current actor; never accept a caller-provided UID as proof of identity.
3. A user can have at most one like per reel and one follow relationship per user pair.
4. Reel counters are denormalized and must not be freely writable by clients.
5. Reels have explicit visibility and moderation/publication states; a hidden, deleted or pending item must not appear in the public feed.
6. Media upload and Firestore metadata publication are separate steps. Model upload state and cleanup/retry behavior in the application workflow; do not pretend a Firestore write and a Storage upload are one transaction.
7. Deleted users/content need a defined lifecycle for media, comments, notifications, and moderation records.
8. Server-side rules/functions remain the security boundary. Kotlin data classes alone do not protect Firestore or Storage.

## Migration from the current schema

Current reel documents use fields similar to `id`, `videoUrl`, `caption`, `uploadedBy`, `timestamp`, `likes`, and `likedBy`. Keep a compatibility mapper during migration:
- `uploadedBy` -> `ownerUid`
- `timestamp` -> `createdAtMillis` / server timestamp
- `videoUrl` -> media bucket/object path (requires parsing or a migration; don't assume every URL is parseable)
- `likedBy` -> `reels/{reelId}/likes/{uid}` relationship records
- Existing numeric `likes` -> initial counter, then reconcile against canonical like records

Do not rename production fields or delete `likedBy` until a migration is tested and both old/new clients' behavior is understood. If data already exists, migrate with a script or trusted backend job, validate counts, and only then switch readers/writers.

## Implementation order

1. Freeze this contract and confirm product scope.
2. Add pure Kotlin domain models separately from Firestore DTOs.
3. Add repository mappers and tests for legacy documents.
4. Add Firestore indexes/rules and Supabase policies alongside repository implementation.
5. Migrate one feature at a time; do not convert every screen at once.

## Non-goals of this change

This document does not change Firestore rules, Supabase policies, existing production documents, upload behavior, or app screens. Those require separate reviewed changes.
