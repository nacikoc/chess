#!/usr/bin/env bash
# APK/AAB içindeki tüm yerel kütüphanelerin 16 KB sayfa boyutuna hizalı olup
# olmadığını denetler. Android 15+ hedefleyen uygulamalarda Play bunu zorunlu
# tutar; hizasız bir .so yükleme sırasında reddedilir.
#
# Kullanım:  tools/check-16kb.sh app/build/outputs/apk/release/app-arm64-v8a-release-unsigned.apk
set -euo pipefail

APK="${1:?kullanim: check-16kb.sh <apk|aab>}"
NDK_ROOT="${ANDROID_NDK_HOME:-${LOCALAPPDATA:-$HOME}/Android/Sdk/ndk}"
READELF="$(find "$NDK_ROOT" -name 'llvm-readelf*' -type f 2>/dev/null | head -1)"
[ -n "$READELF" ] || { echo "llvm-readelf bulunamadi (NDK kurulu mu?)"; exit 2; }

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# Joker desenle cikarma Git Bash'in unzip'inde calismiyor; once adlari
# listeleyip her dosyayi tam adiyla cikariyoruz.
mapfile -t LIBS < <(unzip -Z1 "$APK" | grep '\.so$' || true)
[ "${#LIBS[@]}" -gt 0 ] || { echo "Pakette .so bulunamadi"; exit 2; }

fail=0
for entry in "${LIBS[@]}"; do
  unzip -q -o "$APK" "$entry" -d "$TMP"
  # Her LOAD segmentinin hizalamasi >= 0x4000 olmali
  bad="$("$READELF" -l "$TMP/$entry" 2>/dev/null \
        | awk '/LOAD/ { a=$NF; if (a != "0x4000" && a != "0x10000") print a }' | head -1)"
  if [ -n "$bad" ]; then
    printf 'HIZASIZ  %s  (LOAD align %s)\n' "$entry" "$bad"
    fail=1
  else
    printf 'tamam    %s\n' "$entry"
  fi
done

if [ "$fail" = 1 ]; then
  echo
  echo "En az bir kutuphane 16 KB hizali degil — Play yuklemesi reddedilir."
  exit 1
fi
echo
echo "Tum kutuphaneler 16 KB hizali."
