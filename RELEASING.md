# Yayın süreci

## 1. İmzalama anahtarı (bir kereye mahsus)

> **Bu anahtarı kaybedersen uygulamayı bir daha güncelleyemezsin.** Play'de
> yayımlanan bir uygulamanın imzası değiştirilemez. Anahtarı ve parolasını
> en az iki ayrı yerde yedekle (parola yöneticisi + şifreli yedek).

Proje kökünde çalıştır:

```bash
keytool -genkeypair -v -keystore chess-release.jks -keyalg RSA -keysize 4096 -validity 10000 -alias chess
```

Sorulan parolayı ve bilgileri gir. Sonra proje kökünde `keystore.properties`
dosyasını oluştur (bu dosya `.gitignore`'dadır, depoya girmez):

```properties
storeFile=chess-release.jks
storePassword=BURAYA_STORE_PAROLASI
keyAlias=chess
keyPassword=BURAYA_ANAHTAR_PAROLASI
```

Dosya yoksa release derlemesi imzasız üretilir; debug derlemesi etkilenmez.

Play Console'da **Play App Signing**'i açman önerilir: yükleme anahtarın
çalınırsa değiştirilebilir, Google dağıtım imzasını kendi tutar.

## 2. Sürüm numarası

`app/build.gradle.kts` içinde her yüklemede `versionCode` bir artmalı:

```kotlin
versionCode = 2
versionName = "1.1"
```

Play aynı `versionCode` ile ikinci bir yükleme kabul etmez.

## 3. AAB üret

```bash
gradlew.bat :app:bundleRelease
```

Çıktı: `app/build/outputs/bundle/release/app-release.aab`

Play Console'a **bu dosyayı** yükle (APK değil). Play, cihaz başına yalnızca
uygun ABI'yi indirtir; böylece kullanıcı 3 Stockfish binary'sini birden
indirmez.

### Neden `useLegacyPackaging = true`?

Stockfish ayrı bir süreç olarak çalıştırıldığı için binary'nin diskte gerçek
bir dosya olması gerekir. Bu ayar `android:extractNativeLibs="true"` üretir ve
kurulumda kütüphaneyi diske çıkarır. **Kapatılmamalı** — kapatılırsa
`StockfishEngine` çalışamaz ve uygulama sessizce zayıf `BuiltInEngine`'e düşer.

## 4. Yükleme öncesi kontrol listesi

- [ ] `versionCode` artırıldı
- [ ] Release derlemesi gerçek cihazda denendi (R8 bir şeyi bozmuş olabilir)
- [ ] Gizlilik politikası URL'i yayında ([docs/privacy.html](docs/privacy.html))
- [ ] Veri güvenliği formu dolduruldu (bkz. [docs/play-data-safety.md](docs/play-data-safety.md))
- [ ] İçerik derecelendirme anketi dolduruldu
- [ ] Ekran görüntüleri: telefon + Android TV
- [ ] TV için: 1280x720 banner, leanback beyanı (manifest'te mevcut)
- [ ] Kaynak kod deposu herkese açık (GPLv3 yükümlülüğü)

## 5. Release derlemesini yerelde test etme

İmzasız release APK'yı denemek için debug anahtarıyla imzala:

```bash
gradlew.bat :app:assembleRelease
```

Sonra `build-tools/<sürüm>/apksigner` ile `~/.android/debug.keystore`
(parola: `android`) kullanarak imzalayıp kur. Play'e **debug anahtarıyla
imzalanmış** bir paket yüklenemez; bu yalnızca yerel testler içindir.
