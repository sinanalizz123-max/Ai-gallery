#!/usr/bin/env sh
set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
UPSTREAM_DIR="$ROOT_DIR/upstream"
REPO_DIR="${1:-}"

if [ -z "$REPO_DIR" ]; then
  echo "Usage: $0 /path/to/MobileFaceNet_TF_or_checkpoint_dir"
  exit 1
fi

if [ ! -d "$UPSTREAM_DIR" ]; then
  echo "Missing upstream converter repo. Clone it first:"
  echo "  git clone https://github.com/andrewpks/convert-tensorflow-mobilefacenet-model-to-tflite $UPSTREAM_DIR"
  exit 1
fi

# Resolve checkpoint directory
if [ -d "$REPO_DIR/arch/pretrained_model" ]; then
  CKPT_SRC="$REPO_DIR/arch/pretrained_model"
elif [ -f "$REPO_DIR/checkpoint" ]; then
  CKPT_SRC="$REPO_DIR"
else
  echo "Could not find a checkpoint dir. Provide either:"
  echo "  - MobileFaceNet_TF repo (expects arch/pretrained_model), or"
  echo "  - A checkpoint directory containing a 'checkpoint' file."
  exit 1
fi

# Patch converter input name to 'input' (spec requirement)
if [ -f "$UPSTREAM_DIR/ckpt2tflite.py" ]; then
  sed -i "s/name='img_inputs'/name='input'/g" "$UPSTREAM_DIR/ckpt2tflite.py"
  sed -i "s/inputs=\[\"img_inputs\"\]/inputs=\[\"input\"\]/g" "$UPSTREAM_DIR/ckpt2tflite.py"
fi

DEST="$UPSTREAM_DIR/output/ckpt_best/mobilefacenet_best_ckpt"
mkdir -p "$DEST"

# Copy checkpoint files into expected path
cp -f "$CKPT_SRC"/* "$DEST"/

echo "Checkpoint copied to $DEST"

# Run conversion
(cd "$UPSTREAM_DIR" && python3 ckpt2tflite.py)

OUT_DIR="$UPSTREAM_DIR/output/ckpt_best/mobilefacenet_best_ckpt_evl"
TFLITE_FILE=$(ls "$OUT_DIR"/*.tflite 2>/dev/null | head -n 1)
if [ -z "$TFLITE_FILE" ]; then
  echo "No .tflite output found in $OUT_DIR"
  exit 1
fi

APP_ASSETS="$ROOT_DIR/../../ai/src/main/assets/models"
mkdir -p "$APP_ASSETS"
cp -f "$TFLITE_FILE" "$APP_ASSETS/mobilefacenet.tflite"

echo "Copied model to $APP_ASSETS/mobilefacenet.tflite"
