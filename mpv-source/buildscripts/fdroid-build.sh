#!/bin/bash
set -euo pipefail

# F-Droid invokes this script after preloading every source dependency into deps/.
# No network access is permitted or required here.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

. ./include/depinfo.sh
cores=${cores:-2}
ndk_triple=""
. ./include/path.sh

: "${ANDROID_NDK_ROOT:=$SCRIPT_DIR/sdk/android-ndk-${v_ndk}}"
if [[ ! -d "$ANDROID_NDK_ROOT" ]]; then
  echo "Missing prepared Android NDK: $ANDROID_NDK_ROOT" >&2
  exit 1
fi

# F-Droid supplies the dependencies through srclibs. buildall.sh builds the
# complete dependency graph from those local trees and never calls download.sh.
./buildall.sh --arch arm64 mpv

prefix="$SCRIPT_DIR/prefix/arm64/usr/local"
if [[ ! -f "$prefix/lib/libmpv.so" ]]; then
  echo "libmpv.so was not produced in $prefix/lib" >&2
  exit 1
fi

mpv_root="$SCRIPT_DIR/.."
project_root="$mpv_root/.."
native_libs="$mpv_root/src/main/native-libs/arm64-v8a"
native_headers="$mpv_root/src/main/native-headers/arm64-v8a"
jni_libs="$mpv_root/src/main/jniLibs/arm64-v8a"
rm -rf "$native_libs" "$native_headers" "$jni_libs"
mkdir -p "$native_libs" "$native_headers" "$jni_libs"

# Copy every shared library produced in the reviewed source prefix. This keeps
# the complete DT_NEEDED closure together instead of maintaining a fragile
# hand-written allowlist as codecs and text-rendering libraries change.
shopt -s nullglob
produced_libs=("$prefix"/lib/*.so "$prefix"/lib/*.so.*)
if (( ${#produced_libs[@]} == 0 )); then
  echo "No shared libraries were produced in $prefix/lib" >&2
  exit 1
fi
for library in "${produced_libs[@]}"; do
  cp -L -f "$library" "$native_libs/$(basename "$library" | sed -E 's/\.so\..*$/.so/')"
done
cp -a "$prefix/include/." "$native_headers/"

# Link the JNI bridge against the staged source-built libraries for one ABI.
"$ANDROID_NDK_ROOT/ndk-build" -C "$mpv_root/src/main" \
  APP_ABI=arm64-v8a APP_STL=c++_shared -j2

# Package the JNI bridge and all reviewed source-built libraries into the app.
cp -L -f "$mpv_root/src/main/libs/arm64-v8a/libplayer.so" "$jni_libs/"
for library in "$native_libs"/*.so; do
  cp -L -f "$library" "$jni_libs/"
done

# Package the NDK C++ runtime required by libplayer.so.
stl_library="$(find "$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64" -type f -path '*/aarch64-linux-android/*' -name libc++_shared.so -print -quit)"
if [[ -z "$stl_library" ]]; then
  echo "Missing libc++_shared.so in the prepared NDK" >&2
  exit 1
fi
cp -L -f "$stl_library" "$jni_libs/libc++_shared.so"

test -f "$jni_libs/libplayer.so"
test -f "$jni_libs/libmpv.so"
test -f "$jni_libs/libc++_shared.so"
