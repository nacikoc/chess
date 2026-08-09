# Satranç

Reklamsız, izinsiz, tamamen çevrimdışı satranç uygulaması. Tek APK ile hem
Android telefonlarda hem Android TV'de (kumanda/D-pad desteğiyle) çalışır.

## Özellikler

- **Bilgisayara karşı**: 8 zorluk seviyesi, beyaz/siyah seçimi
- **İki kişilik**: aynı cihazda karşılıklı oyun
- Hamle listesi (SAN), geri alma, tahta çevirme, terfi seçimi
- Şah/mat/pat/beraberlik tespiti

## Mimari

- **UI**: Kotlin + Jetpack Compose (Material 3). Telefonda dikey, TV/yatayda
  tahta solda + panel sağda düzeni. TV için leanback launcher + D-pad odak
  desteği (`MainActivity`, `ui/`).
- **Kurallar**: [chesslib](https://github.com/bhlangonijr/chesslib) — hamle
  üretimi, rok, en passant, terfi, mat/pat/beraberlik.
- **Yapay zekâ** (`engine/`):
  - `StockfishEngine`: resmi Stockfish 17.1 Android (arm64) binary'si
    `jniLibs/arm64-v8a/libstockfish.so` olarak paketlenir, ayrı süreçte
    çalıştırılıp UCI protokolüyle konuşulur. Zorluk = Skill Level + süre.
  - `BuiltInEngine`: Stockfish binary'si olmayan ABI'larda (x86_64 emülatör,
    32-bit cihazlar) devreye giren Kotlin alpha-beta motoru.
  - Seçim `EngineFactory` içinde çalışma zamanında yapılır.

## Derleme

```
gradlew.bat :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Not: `libstockfish.so` (76 MB) depoya dahildir; Stockfish GPLv3 lisanslıdır
(`https://github.com/official-stockfish/Stockfish`). Uygulama Play'e
yüklenecekse GPL yükümlülükleri dikkate alınmalıdır. Taş glifleri için DejaVu
Sans (`res/font/chess_font.ttf`) paketlenmiştir.
