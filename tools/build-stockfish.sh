#!/usr/bin/env bash
# Stockfish'i Android icin 16 KB sayfa boyutuna hizali olarak derler ve
# app/src/main/jniLibs/<abi>/libstockfish.so olarak yerlestirir.
#
# NEDEN KENDIMIZ DERLIYORUZ: Stockfish'in resmi Android binary'lerinin (17.1 ve
# 18 dahil) LOAD segmentleri 4 KB hizali. Android 15+ hedefleyen uygulamalarda
# Google Play 16 KB hizalama zorunlu tuttugu icin resmi binary ile yukleme
# reddediliyor. Kaynaktan derleyip -Wl,-z,max-page-size=16384 vermek sorunu
# cozuyor; kod degistirilmiyor.
#
# Gereksinimler: Android NDK r27c+ (r28 onerilir), bash, curl, tar.
# Kullanim:  tools/build-stockfish.sh [surum-etiketi]     (varsayilan: sf_17.1)
set -euo pipefail

SF_TAG="${1:-sf_17.1}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NDK_ROOT="${ANDROID_NDK_HOME:-${LOCALAPPDATA:-$HOME}/Android/Sdk/ndk}"
NDK="$(find "$NDK_ROOT" -maxdepth 1 -mindepth 1 -type d 2>/dev/null | sort -V | tail -1)"
[ -n "$NDK" ] || { echo "NDK bulunamadi: $NDK_ROOT"; exit 2; }

case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) HOST=windows-x86_64 ;;
  Darwin)               HOST=darwin-x86_64 ;;
  *)                    HOST=linux-x86_64 ;;
esac
export PATH="$NDK/toolchains/llvm/prebuilt/$HOST/bin:$NDK/prebuilt/$HOST/bin:$PATH"

ALIGN="-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo ">> $SF_TAG kaynagi indiriliyor"
curl -sL -o "$WORK/sf.tar" \
  "https://github.com/official-stockfish/Stockfish/releases/download/$SF_TAG/stockfish-android-armv8.tar"
tar -xf "$WORK/sf.tar" -C "$WORK"
SRC="$WORK/stockfish/src"

build() {
  local arch="$1" abi="$2" cxx="$3"
  echo ">> $abi derleniyor ($arch)"
  ( cd "$SRC" && make clean >/dev/null 2>&1 || true )
  # COMPCXX aciktan veriliyor: Makefile'in API seviyesi hesabi Windows'ta
  # bozulup var olmayan bir derleyici adi (androideabi16) uretebiliyor.
  ( cd "$SRC" && make -j"$(nproc 2>/dev/null || echo 4)" build \
      ARCH="$arch" COMP=ndk COMPCXX="$cxx" EXTRALDFLAGS="$ALIGN" >/dev/null )
  mkdir -p "$REPO_ROOT/app/src/main/jniLibs/$abi"
  llvm-strip -s "$SRC/stockfish" -o "$REPO_ROOT/app/src/main/jniLibs/$abi/libstockfish.so"
  local bad
  bad="$(llvm-readelf -l "$REPO_ROOT/app/src/main/jniLibs/$abi/libstockfish.so" \
        | awk '/LOAD/ { if ($NF != "0x4000" && $NF != "0x10000") print $NF }' | head -1)"
  [ -z "$bad" ] || { echo "HATA: $abi hizali degil ($bad)"; exit 1; }
  echo "   tamam: $abi ($(stat -c%s "$REPO_ROOT/app/src/main/jniLibs/$abi/libstockfish.so") bayt, 16 KB hizali)"
}

build armv8 arm64-v8a   aarch64-linux-android21-clang++
build armv7 armeabi-v7a armv7a-linux-androideabi21-clang++

echo
echo "Bitti. Dogrulamak icin:  tools/check-16kb.sh <apk>"
