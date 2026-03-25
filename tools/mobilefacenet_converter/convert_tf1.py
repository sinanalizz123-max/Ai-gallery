#!/usr/bin/env python3
import argparse
import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
UPSTREAM = ROOT / "upstream"


def patch_input_name(ckpt_script: Path) -> None:
    text = ckpt_script.read_text()
    text = text.replace("name='img_inputs'", "name='input'")
    text = text.replace("inputs=[\"img_inputs\"]", "inputs=[\"input\"]")
    ckpt_script.write_text(text)


def main() -> int:
    parser = argparse.ArgumentParser(description="Convert MobileFaceNet TF checkpoint to TFLite")
    parser.add_argument("--checkpoint", required=True, help="Path to MobileFaceNet checkpoint dir")
    args = parser.parse_args()

    if not UPSTREAM.exists():
        print("Missing upstream converter repo. Clone it first:")
        print(f"  git clone https://github.com/andrewpks/convert-tensorflow-mobilefacenet-model-to-tflite {UPSTREAM}")
        return 1

    ckpt_src = Path(args.checkpoint).resolve()
    if not ckpt_src.is_dir():
        print("Checkpoint path must be a directory.")
        return 1

    if (ckpt_src / "arch" / "pretrained_model").is_dir():
        ckpt_src = ckpt_src / "arch" / "pretrained_model"

    if not (ckpt_src / "checkpoint").exists():
        print("Checkpoint dir must contain a 'checkpoint' file.")
        return 1

    ckpt_script = UPSTREAM / "ckpt2tflite.py"
    patch_input_name(ckpt_script)

    dest = UPSTREAM / "output" / "ckpt_best" / "mobilefacenet_best_ckpt"
    dest.mkdir(parents=True, exist_ok=True)
    for item in ckpt_src.iterdir():
        if item.is_file():
            shutil.copy2(item, dest / item.name)

    subprocess.check_call([sys.executable, str(ckpt_script)], cwd=str(UPSTREAM))

    out_dir = UPSTREAM / "output" / "ckpt_best" / "mobilefacenet_best_ckpt_evl"
    tflite_files = list(out_dir.glob("*.tflite"))
    if not tflite_files:
        print(f"No .tflite output found in {out_dir}")
        return 1

    app_assets = ROOT.parent.parent / "ai" / "src" / "main" / "assets" / "models"
    app_assets.mkdir(parents=True, exist_ok=True)
    shutil.copy2(tflite_files[0], app_assets / "mobilefacenet.tflite")
    print(f"Copied model to {app_assets / 'mobilefacenet.tflite'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
