# Mpv∞ source-built MPV module

This module is an isolated source-build boundary for the future `store` flavor. The existing `standard`, `noVulkan`, and `fongmi` flavors continue to consume their current AAR files and are not replaced by this module.

## Compatibility boundary

The app-facing API remains the static `is.xyz.mpv.MPVLib` contract used by `PlaybackSession`: `create`, option writes, `init`, surface attachment, property access, node commands, observers, and destroy. The native lifecycle therefore remains **create → configure → init**. `MPVNode` is compiled from the source module and node conversion is implemented in `src/main/jni/node.cpp`.

The unpublished historical `grabThumbnailFast` extension is not copied into this module. The store module exposes the same call surface through a platform `MediaMetadataRetriever` implementation and keeps the existing coroutine `FastThumbnails` API for cached and seek thumbnails. This is a compatibility implementation that requires device validation for local files, SAF content URIs, and network sources before it can be treated as feature-equivalent.

## Native source and pinning

The exploratory native source and build scripts are derived from the following immutable Git commits and must be reviewed against the eventual F-Droid recipe before metadata is submitted:

| Component | Repository | Commit | Role |
|---|---|---|---|
| Static JNI/libmpv Android integration | `jmir1/mpv-android` | `da7c885c18ae117ca27239c562ea7572fce3d4f5` | Static `MPVLib`, JNI bridge, native build scripts |
| MPVNode model and node conversion reference | `abdallahmehiz/mpv-android` | `18e41158e1ad24c1819598be15f51c898397e04f` | Kotlin `MPVNode` API and node conversion reference |
| Original upstream lineage | `mpv-android/mpv-android` | To be pinned in the final recipe | libmpv Android source lineage |

The copied `buildscripts` currently use their own pinned dependency variables, but their download workflow is still an exploratory developer build path. It is **not yet the final F-Droid recipe**: F-Droid metadata must use reviewed, pinned source dependencies and must not depend on an opaque prebuilt AAR or an unreviewed runtime download.

## Build flow

The native prefix is built first for each selected ABI using the reviewed upstream scripts. `ndk-build` then links the JNI bridge against the generated libmpv/FFmpeg prefix and places the resulting JNI libraries under `src/main/libs`. Gradle packages those libraries into the `mpv-source` Android library, which is consumed only through `storeImplementation(project(":mpv-source"))`.

The first validation target is a **Debug store build**. Release signing and public release publication are intentionally out of scope until the source build has compiled in CI and passed user device testing. Stable public `v1.0.3` remains unchanged.

## Known policy and parity gates

This module alone does not establish F-Droid or IzzyOnDroid compliance. The complete app still requires an audit of Cast, Media3 FFmpeg, libtorrent4j, curl, MediaInfo, runtime yt-dlp behavior, licenses, dependency sources, and final APK size. Any store-only feature difference, including a possible Cast omission, must be represented honestly in the final metadata and release notes.
