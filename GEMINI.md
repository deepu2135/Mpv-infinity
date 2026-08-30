# Repository Guidelines & Rules for Mpv-infinity

## 1. Branding & Identity
- The application display name is **Mpv** (without `∞` or `-Debug` suffixes) across all languages and metadata.
- The standard application ID is `app.infinity.mpvz`.

## 2. Torrent & Streaming Engine
- When exiting or stopping torrent playback in `PlayerActivity`, always immediately delete temporary torrent cache files.
- Ensure stale torrent cache directories are automatically cleaned up on app launch and during background maintenance.
- Preserve sliding buffer window mechanics (`BUFFER_WINDOW_BYTES` and `READ_AHEAD_BYTES`) to avoid excessive memory or disk accumulation.

## 3. Media & Chapter Support
- Preserve chapter metadata parsing across both **MPV** (`ChapterNode` with safe null/default handling) and **Media3** (`ChapterFrame`, `Chapter`).
- Always support chapter navigation and interactive chapter chips/sheets for audiobooks and video files.

## 4. Build & Performance Standards
- CI/CD builds must default to **Release** mode (`assembleStandardRelease`) with R8 minification (`isMinifyEnabled = true`) and resource shrinking (`isShrinkResources = true`) to ensure instant cold startup and 60/120 FPS smoothness.
- All release artifacts in CI must be signed with the repository debug keystore (`app/debug.keystore`) to prevent package conflict errors.
- Keep direct download links updated in release descriptions.
