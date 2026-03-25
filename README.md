# SmartGallery AI

Modern Android gallery app with offline-first AI face recognition and interactive learning.

## Summary
This project is a full Android gallery app that:
- Loads images and videos from MediaStore.
- Runs offline face detection + embedding.
- Saves embeddings to Room.
- Groups similar faces into People clusters.
- Shows face boxes on photo detail.
- Provides photo actions (rename/details/delete).
- Includes an AI Training screen placeholder for future review pairs.

## Features Implemented
- MediaStore-backed gallery with paging and permissions gate.
- 3-column photo grid with rounded corners and overlays.
- Full-resolution photo detail view (not just thumbnails).
- Face bounding boxes drawn over photo detail.
- People tab backed by Room clustering results.
- Unknown faces section (people marked `isUnknown`).
- Person detail: rename, merge, remove selected photos.
- Photo actions: Rename, Details, Delete via MediaStore.
- Background scanning (WorkManager) for new media items.
- Cluster grouping from embeddings using squared Euclidean distance threshold.
- Model pipeline: MediaPipe face detection + MobileFaceNet embeddings.
- L2 normalized embeddings for efficient similarity.
- No demo people names or demo suggestions (removed Ali/Pet placeholders).

## Current Limitations
- AI training queue is empty until we generate review pairs.
- Settings actions (re-scan / clear AI data) are not wired yet.
- MediaStore delete/rename may require user consent on some devices (scoped storage).
- Albums screen is still demo data (AI/system albums not derived yet).

## Modules
- `app`: Application entry point, DI container, WorkManager scheduling.
- `ui`: Jetpack Compose UI, navigation, view models, screens.
- `data`: Models, repositories, Room entities/DAO, paging sources.
- `ai`: Face detection/recognition pipeline, clustering logic, workers.

## Data Flow (High Level)
1. WorkManager scans new media items.
2. Face detector finds faces.
3. Faces are aligned, cropped, resized, normalized.
4. MobileFaceNet generates embeddings (L2 normalized).
5. Embeddings are stored in Room.
6. Grouping logic clusters embeddings into People.
7. People and face boxes are shown in UI.

## AI Pipeline Details
- Face detector: MediaPipe Tasks Vision.
- Face recognizer: MobileFaceNet (TFLite).
- Alignment: eye landmarks used to rotate face before cropping.
- Input size: 160x160.
- Normalization: (pixel - 127.5) / 128.0.
- Output: L2-normalized embedding vector.
- Comparator: Squared Euclidean distance, threshold 1.0.

## Models (Assets)
Place these into `ai/src/main/assets/models/` for local builds:
- `face_detection_short_range.tflite`
- `mobilefacenet.tflite`

## Room Schema (Relevant)
- `people`: person id, name, isUnknown, updatedAt.
- `embeddings`: mediaId, personId, vector, confidence, bounding box coords.
- `corrections`: user correction history (reserved for future use).

## WorkManager
- Periodic scan scheduled on app start.
- Only runs with media permissions granted.

## CI / Build
- GitHub Actions builds **debug** APK and uploads it as artifact.
- Workflow downloads models into assets during build.
- Debug signing only (no release keystore required).

## Project Files You’ll Commonly Touch
- `app/src/main/java/com/smartgallery/ai/MainActivity.kt`
- `app/src/main/java/com/smartgallery/ai/SmartGalleryApplication.kt`
- `app/src/main/java/com/smartgallery/ai/di/AppContainer.kt`
- `ai/src/main/java/com/smartgallery/ai/engine/FaceEmbeddingPipeline.kt`
- `ai/src/main/java/com/smartgallery/ai/engine/MediaPipeFaceDetector.kt`
- `ai/src/main/java/com/smartgallery/ai/engine/TfliteFaceRecognizer.kt`
- `ai/src/main/java/com/smartgallery/ai/engine/GroupingLogic.kt`
- `ai/src/main/java/com/smartgallery/ai/workers/FaceRecognitionWorker.kt`
- `data/src/main/java/com/smartgallery/data/db/AppDatabase.kt`
- `data/src/main/java/com/smartgallery/data/repository/impl/MediaStoreMediaRepository.kt`
- `data/src/main/java/com/smartgallery/data/repository/impl/RoomPeopleRepository.kt`
- `ui/src/main/java/com/smartgallery/ui/screens/PhotosScreen.kt`
- `ui/src/main/java/com/smartgallery/ui/screens/PeopleScreen.kt`
- `ui/src/main/java/com/smartgallery/ui/screens/PhotoDetailScreen.kt`

## How To Build Locally
```sh
./gradlew :app:assembleDebug
```

## Suggested Next Steps
1. Build real AI Training pairs (uncertain matches) and feed AI tab.
2. Wire Settings actions (re-scan / clear AI data).
3. Replace demo albums with MediaStore + AI derived albums.
4. Add photo metadata edit (tags, notes).
