# Üçüncü taraf bileşenler

Bu uygulama aşağıdaki özgür yazılım bileşenlerini kullanır.

## Stockfish 17.1

- Telif: The Stockfish developers
- Lisans: GNU General Public License v3.0
- Kaynak: https://github.com/official-stockfish/Stockfish

Stockfish, `app/src/main/jniLibs/<abi>/libstockfish.so` adıyla paketlenmiştir
(arm64-v8a ve armeabi-v7a). Ayrı bir süreç olarak çalıştırılır ve UCI
protokolüyle konuşulur.

**Kaynak kodda hiçbir değişiklik yapılmamıştır**, ancak binary resmi sürümden
alınmayıp `sf_17.1` etiketli kaynaktan Android NDK ile yeniden derlenmiştir.
Tek fark bağlayıcı bayraklarıdır:

```
-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384
```

Gerekçe: Stockfish'in resmi Android binary'lerinde (17.1 ve 18 dahil) LOAD
segmentleri 4 KB hizalıdır. Android 15+ hedefleyen uygulamalarda Google Play
16 KB sayfa boyutu desteğini zorunlu tuttuğu için resmi binary ile yükleme
reddedilir.

Derlemeyi birebir tekrarlamak için: [`tools/build-stockfish.sh`](tools/build-stockfish.sh)
Sonucu doğrulamak için: [`tools/check-16kb.sh`](tools/check-16kb.sh)

**Stockfish GPLv3 olduğu için bu uygulamanın tamamı da GPLv3 ile lisanslanmıştır**
(bkz. [LICENSE](LICENSE)). Kaynak kodun tamamı şu adreste bulunur:
https://github.com/nacikoc/chess

## chesslib 1.3.4

- Telif: Ben-Hur Carlos Vieira Langoni Junior
- Lisans: Apache License 2.0
- Kaynak: https://github.com/bhlangonijr/chesslib

Satranç kuralları, hamle üretimi ve pozisyon değerlendirmesi için kullanılır.

## DejaVu Sans

- Lisans: DejaVu Fonts License (Bitstream Vera türevi, izin verici)
- Kaynak: https://dejavu-fonts.github.io/

`app/src/main/res/font/chess_font.ttf` olarak paketlenmiştir; satranç taşı
glifleri (♔♕♖♗♘♙ …) ve arayüzdeki ok simgeleri bu yazı tipinden gelir.

## Jetpack Compose, AndroidX

- Telif: The Android Open Source Project
- Lisans: Apache License 2.0
