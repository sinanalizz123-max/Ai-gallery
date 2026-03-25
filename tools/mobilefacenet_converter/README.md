# MobileFaceNet Converter Flow (sirius-ai/MobileFaceNet_TF)

This wiring uses the community converter script `ckpt2tflite.py` to turn a **TensorFlow 1.x checkpoint** into a `.tflite` embedding model.

## 1) Get the MobileFaceNet_TF checkpoint
The `sirius-ai/MobileFaceNet_TF` README links to a `pretrained_model` folder. Clone the repo and check `arch/pretrained_model`.

If you don’t see checkpoint files after cloning, run `git lfs pull` (large files may use LFS).

## 2) Clone the converter repo
```bash
cd tools/mobilefacenet_converter

git clone https://github.com/andrewpks/convert-tensorflow-mobilefacenet-model-to-tflite upstream
```

## 3) Create a Python environment (TF1.x)
```bash
cd tools/mobilefacenet_converter
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## 4) Run the converter (choose one)

### Option A: Shell wrapper
```bash
./convert.sh /path/to/MobileFaceNet_TF
```

### Option B: Python wrapper
```bash
python3 convert_tf1.py --checkpoint /path/to/MobileFaceNet_TF
```

Both methods:
- Patch the converter to use input name `input` and output `embeddings`.
- Copy the checkpoint into the converter’s expected path.
- Run `ckpt2tflite.py`.
- Copy the result to `ai/src/main/assets/models/mobilefacenet.tflite`.

## Model Specs
- Input array name: `input`
- Input shape: `1, 112, 112, 3`
- Output array name: `embeddings`
- Standardization: `(img - 127.5) / 128.0`

The app’s `TfliteFaceRecognizer` already applies the standardization and L2-normalizes the output.
