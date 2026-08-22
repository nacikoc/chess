# Satranç

Reklamsız, takipsiz, tamamen çevrimdışı bir satranç uygulaması. Tek kod
tabanıyla Android telefon, tablet ve Android TV'de (kumanda/D-pad desteğiyle)
çalışır.

Geliştiren: **Naci Koç** · İletişim: <nakisoft@gmail.com> · Paket adı:
`com.hilspot.chess`

> Bu uygulama, Play Store'daki satranç oyunlarının reklam yoğunluğundan
> rahatsız olup "reklamsız olsun" diye başlanmış kişisel bir projedir.

## Özellikler

- **Bilgisayara karşı** — Stockfish 17.1, 8 zorluk seviyesi (Acemi → Büyükusta).
  Alt seviyelerde arama derinliği kasıtlı olarak sınırlandırılmıştır, böylece
  motor gerçekten hata yapar ve sıradan bir oyuncu tarafından yenilebilir.
  Varsayılan seviye: Normal.
- **İki kişilik** — aynı cihazda karşılıklı oyun. Her hamleden sonra tahta
  sırası gelen oyuncuya döner; TV'de imleç de o oyuncunun tarafına atlar.
- Oyuncu kartları: alınan taşlar, materyal farkı, sıra ve şah göstergesi
- Hamle listesi (SAN), geri alma, tahta çevirme, terfi seçimi
- Şah / mat / pat / beraberlik tespiti, oyun sonu perdesi
- Hesap yok, veri toplanmaz, oyunun hiçbir yeri internete çıkmaz. (İnternet
  izni yalnızca Destek Ol ekranındaki Google Play faturalandırması için
  bulunur; Play Billing kütüphanesi manifest'e `INTERNET` ve
  `ACCESS_NETWORK_STATE` ekler.)

## Mimari

- **UI** — Kotlin + Jetpack Compose (Material 3).
  - Telefonda dikey düzen: tahta ve oyuncu kartları alta yaslanır, cihazdan
    cihaza değişen fazla yükseklik tek bir esnek banda düşer; kontroller
    başparmak menzilinde yuvarlak bir yuvada durur.
  - TV/tablette yatay düzen: tahta solda, panel sağda.
  - `FocusButton` D-pad odağını belirgin gösterir (camgöbeği çerçeve + büyüme).
- **Kurallar** — [chesslib](https://github.com/bhlangonijr/chesslib): hamle
  üretimi, rok, en passant, terfi, mat/pat/beraberlik.
- **Yapay zekâ** (`engine/`)
  - `StockfishEngine` — resmi Stockfish Android binary'si `jniLibs/<abi>/
    libstockfish.so` olarak paketlenir, ayrı süreçte çalıştırılıp UCI
    protokolüyle konuşulur. Zorluk = Skill Level + arama sınırı.
  - `BuiltInEngine` — Stockfish binary'si olmayan ABI'larda (x86_64 emülatör,
    Chromebook) devreye giren Kotlin alpha-beta motoru.
  - Seçim `EngineFactory` içinde çalışma zamanında yapılır.

## Derleme

```
gradlew.bat :app:assembleDebug
```

Üretilen APK'lar `app/build/outputs/apk/debug/` altındadır (arm64-v8a,
armeabi-v7a, x86_64). Cihaza kurmak için:

```
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

Yayın derlemesi için bkz. [RELEASING.md](RELEASING.md).

## Lisans

Bu program özgür yazılımdır: GNU Genel Kamu Lisansı sürüm 3 (veya, tercihinize
göre, daha sonraki bir sürümü) koşulları altında yeniden dağıtabilir ve/veya
değiştirebilirsiniz. Tam metin: [LICENSE](LICENSE).

Uygulama, GPLv3 lisanslı **Stockfish** satranç motorunu içerdiği için bir bütün
olarak GPLv3 ile lisanslanmıştır. Kullanılan diğer bileşenler ve lisansları:
[THIRD-PARTY.md](THIRD-PARTY.md).

Bu program hiçbir garanti verilmeksizin, faydalı olacağı umuduyla dağıtılır;
SATILABİLİRLİK veya BELİRLİ BİR AMACA UYGUNLUK zımni garantisi dahi verilmez.
