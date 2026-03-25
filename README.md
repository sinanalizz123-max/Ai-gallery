# SmartGallery AI

Modern Android gallery app with offline-first AI face recognition and interactive learning.

## Modules
- `app`: Application entry point and dependency wiring
- `ui`: Jetpack Compose UI, navigation, view models
- `data`: Models, repositories, Room entities, paging sources
- `ai`: Face detection/recognition interfaces and local AI engine

## Pipeline (MediaPipe + MobileFaceNet)
The AI module includes a detection + embedding pipeline:
- `MediaPipeFaceDetector` -> detects faces (MediaPipe Tasks Vision model)
- `TfliteFaceRecognizer` -> produces L2-normalized embeddings
- `FaceEmbeddingPipeline` -> crops each face + runs embedding

### Model Placement
Drop these files into `ai/src/main/assets/models/`:
- `face_detection_short_range.tflite`
- `mobilefacenet.tflite`

Update `modelAssetPath` in `MediaPipeFaceDetector` and `TfliteFaceRecognizer` if you rename them.

## MobileFaceNet Conversion Flow
The repo includes a wired conversion guide to turn a TF1.x MobileFaceNet checkpoint into `.tflite`:
- `tools/mobilefacenet_converter/README.md`

## Current Status
- MediaStore-backed gallery (images + videos) with paging + permission gate.
- MediaPipe face detection + TFLite embedding pipeline wired for use.
- UI screens and navigation complete.

## Remaining Extensions
1. Implement clustering + person labeling on top of embeddings.
2. Persist embeddings/corrections in Room and feed the AI training queue.
3. Replace demo People/Albums with derived data.
