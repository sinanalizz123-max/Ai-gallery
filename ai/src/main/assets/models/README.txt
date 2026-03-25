Place the following models in this folder:

1) face_detection_short_range.tflite
   - MediaPipe face detection model.

2) mobilefacenet.tflite
   - MobileFaceNet embedding model (single-face -> embedding vector).

Need help converting a TF checkpoint to `.tflite`?
See tools/mobilefacenet_converter/README.md

Update modelAssetPath in MediaPipeFaceDetector or TfliteFaceRecognizer if you use different names.
