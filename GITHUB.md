# Git + GitHub Quickstart (Termux)

This file is a practical checklist to open this project later and push/pull fast.

## 0) Open Project
```sh
cd ~/smartgallery-ai
```

## 1) Check Git State
```sh
git status -sb
```

## 2) Pull Latest (before edits)
```sh
git pull --rebase origin main
```

## 3) Stage + Commit
```sh
git add -A
git commit -m "Your message"
```

## 4) Push
```sh
git push origin main
```

## 5) See Recent Commits
```sh
git log --oneline -n 10
```

## First Time Setup (Only Once)
### A) Initialize repo + add remote
```sh
cd ~/smartgallery-ai
git init
git branch -M main
git remote add origin https://github.com/sinanalizz123-max/Ai-gallery
```

### B) Authenticate (no username/password)
Use GitHub CLI (recommended):
```sh
pkg install gh -y
gh auth login
```
Pick **HTTPS** and **Paste a token** when asked. Then run:
```sh
git push -u origin main
```

Alternative: SSH (if you already have keys set up)
```sh
git remote set-url origin git@github.com:sinanalizz123-max/Ai-gallery.git
```

## Keys / Tokens (Where They Live)
- GitHub CLI token is stored in:
  - `~/.config/gh/hosts.yml`
- SSH keys (if used) are in:
  - `~/.ssh/id_ed25519` (private)
  - `~/.ssh/id_ed25519.pub` (public)

## GitHub Actions (Build)
- Pushing to `main` triggers the build workflow.
- Download the APK from Actions → latest run → Artifacts.
- The workflow uses **debug signing** (no keystore needed).

## Model Files (Assets)
- Local path:
  - `ai/src/main/assets/models/face_detection_short_range.tflite`
  - `ai/src/main/assets/models/mobilefacenet.tflite`
- CI downloads these models automatically into the same folder.
- Keep paths unchanged.

## Common Fixes
- If push fails due to auth:
```sh
gh auth status
gh auth login
```
- If you have local changes and need latest remote:
```sh
git stash -u
git pull --rebase origin main
git stash pop
```
