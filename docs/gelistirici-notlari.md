# Geliştirici notları

Bu dosya, koda bakarak **anlaşılamayacak** şeyleri anlatır: neden öyle yapıldı,
hangi tuzağa düşüldü, nerede ne duruyor. Projeyi aylar sonra açtığında ya da
başka biri devraldığında ilk okunacak yer burasıdır.

Kodun kendisi için [README](../README.md), yayın süreci için
[RELEASING.md](../RELEASING.md), Play Console adımları için
[play-console-yukleme.md](play-console-yukleme.md).

Son güncelleme: 26 Eylül 2026

---

## 1. Otuz saniyede proje

Reklamsız, çevrimdışı bir Android satranç uygulaması. Kotlin + Jetpack Compose.
Tek kod tabanı telefon, tablet ve Android TV'de çalışır.

| | |
|---|---|
| Paket adı | `com.hilspot.chess` — **değişmez**, Play'e ilk yüklemeden sonra kilitli |
| Geliştirici | Naci Koç · nakisoft@gmail.com |
| Lisans | GPLv3 (Stockfish'i içerdiği için) |
| minSdk / targetSdk | 26 / 36 |
| Motor | Stockfish 17.1, ayrı süreç, UCI protokolü |
| Kurallar | chesslib (Apache 2.0) |

Başlama sebebi: Play Store'da satranç aradığında çıkan oyunların reklam
yoğunluğu. Bu yüzden **reklamsız ve çevrimdışı olmak bu projenin ana ilkesidir** —
yeni bir özellik bu ilkeyi bozuyorsa o özellik yanlıştır.

---

## 2. Nerede ne var

| Ne | Nerede |
|---|---|
| Kaynak kod | https://github.com/nacikoc/chess (public) |
| APK'lar ve motor binary'leri | GitHub → Releases → `v1.0` |
| Tanıtım sayfası | https://nacikoc.github.io/chess/ (GitHub Pages, `/docs` klasöründen) |
| Gizlilik politikası | https://nacikoc.github.io/chess/privacy.html |
| İmzalama anahtarı | `chess-release.jks` — proje kökünde, **gitignore'da** |
| Anahtar yedeği | Masaüstünde `chess-imzalama-anahtari-YEDEK.zip` + Google Drive |
| Anahtar parolası | O ZIP'in içindeki `OKU-BENI.txt` dosyasında (buraya yazılmaz) |
| Eski git geçmişi yedeği | `C:\Users\PC\source\repos\_chess-backup\chess-full-history.bundle` |

**Sertifika parmak izi** (doğrulama için, gizli değil):

```
SHA-256: 12:AF:81:B6:7A:AE:C1:C2:05:F9:A6:12:D4:D8:57:45:60:D9:0E:BD:D6:67:8C:B9:48:D2:D2:50:08:22:57:87
```

> **Bu anahtar kaybolursa uygulama bir daha güncellenemez.** Play'de yayımlanmış
> bir uygulamanın imzası değiştirilemez; tek çare yeni paket adıyla sıfırdan
> yayın olur ve kullanıcılar, yorumlar, indirme sayısı taşınmaz.

---

## 3. Zaman kaybettiren tuzaklar

Bunların hepsi bu projede gerçekten yaşandı. Tekrar düşmemek için yazıldı.

### 3.1 Stockfish'in resmî binary'leri Play'e yüklenemiyor

Resmî Stockfish Android binary'leri (17.1 ve 18 dahil) **4 KB sayfa hizalı**.
Android 15+ hedefleyen uygulamalarda Play 16 KB hizalama şart koşuyor ve bunları
reddediyor. Telefon kurulumda "16 KB ile uyumlu değil" uyarısı veriyor.

**Çözüm:** kaynaktan, NDK ile, `-Wl,-z,max-page-size=16384` bayrağıyla derlemek.
Betik: [`tools/build-stockfish.sh`](../tools/build-stockfish.sh).
Denetim: [`tools/check-16kb.sh`](../tools/check-16kb.sh) — APK **ve** AAB üzerinde çalışır.

İlgili: `androidx.graphics:graphics-path` sürümü **1.1.0'a sabitlendi**. Compose
BOM'un getirdiği 1.0.1 de 16 KB uyumsuz. `1.0.2 diye bir sürüm yok`, aramayın.

Stockfish 18 denendi ama 17.1'den 34 MB büyük olduğu için kullanılmadı.

### 3.2 Play Billing 8'in altını kabul etmiyor

31 Ağustos 2026'dan sonra Billing 8+ zorunlu. Proje 9.1.0'da.

**`billing-ktx` kullanılamıyor:** Kotlin 2.3 metadata'sıyla derlenmiş, projenin
Kotlin 2.1.20'si okuyamıyor ve derleme `Module was compiled with an incompatible
version of Kotlin` ile patlıyor. Düz `billing` yeterli — kod zaten yalnızca geri
çağrı tabanlı Java API'sini kullanıyor, ktx uzantılarına ihtiyacı yok.

Billing 8'deki tek kırıcı değişiklik bizi ilgilendiren tarafta:
`queryProductDetailsAsync` geri çağrısı artık `List<ProductDetails>` değil
`QueryProductDetailsResult` veriyor (`.productDetailsList` ile erişiliyor).

### 3.3 Play başlıkta "Reklamsız" yazdırmıyor

Play'in meta veri politikası **başlık, simge ve geliştirici adında** promosyon
ve fiyat iddialarını yasaklıyor: `reklamsız`, `ad-free`, `free`, `#1`, `en iyi`.
Uygulama doğrudan reddediliyor.

Mağaza adı bu yüzden **"Satranç — Çevrimdışı" / "Chess — Offline"**. Reklamsız
olduğu bilgisi açıklama metninde, orası serbest. Öne çıkan görsel ve TV
banner'ındaki "Reklamsız" ibaresi de aynı sebeple çıkarıldı.

### 3.4 "Bağış" deme, "destek" de

Play'de bağış/donation, vergiden muaf hayır amaçlı ödemeleri anlatır ve kayıtlı
kurum doğrulaması ister. Buradaki ödeme kişisel geliştirici desteğidir, hiçbir
içerik açmaz. Tüm Türkçe metinler bu yüzden "destek/tip" dilinde. Console'da
ürün adlarını girerken de "bağış" yazma.

### 3.5 İç test, kapalı test yerine geçmiyor

13 Kasım 2023'ten sonra açılmış bireysel hesaplar, üretime çıkmadan önce
**12 test kullanıcısıyla 14 gün kesintisiz kapalı test** yürütmek zorunda.
İç test (internal testing) bu şarta **sıfır gün** sayar.

Daha kötüsü: **iç teste katılmış bir kullanıcı kapalı testi alamaz.** Google'ın
belgesi net — önce iç testten çıkması, sonra kapalı teste katılması gerekiyor.
Listeden silmen yetmiyor, kullanıcının kendisi "Leave the program" demeli.
Yani iç test linkini gönderdiğin kişiler 12'nin içinde sayılmaz.

Pratik sonuç: iç testi sadece kendi cihazların için kullan, 12 kişiyi hiç iç
test linki görmemiş kişilerden seç ve 12 değil **16-20 kişi** davet et (%25-35'i
ya katılmıyor ya düşüyor; düşen birinin yerine gelen sıfırdan 14 gün başlatıyor).

### 3.6 AAB ile ABI split'leri birlikte çalışmıyor

`bundleRelease` görevi, ABI split'leri açıkken
`Sequence contains more than one matching element` ile patlıyor. `build.gradle.kts`
bunu `gradle.startParameter.taskNames` içinde "bundle" arayarak çözüyor: AAB
derlerken split'ler kapanıyor. AAB zaten cihaz başına doğru ABI'yi kendi ayırıyor.

### 3.7 Konfigürasyon önbelleği ve `doLast`

Kotlin DSL'de `doLast { }` lambdası betik nesnesini örtük yakalıyor ve
configuration cache bunu serileştiremiyor. Bu yüzden Stockfish varlık kontrolü
ayrı bir Gradle görevi değil, **yapılandırma zamanında** yapılıyor. Yeni bir
kontrol eklerken aynı tuzağa dikkat.

---

## 4. Neden böyle yapıldı

### Stockfish neden ayrı süreç?

`StockfishEngine`, binary'yi `ProcessBuilder` ile **ayrı bir işletim sistemi
süreci** olarak başlatır ve stdin/stdout üzerinden UCI protokolüyle konuşur.
JNI yok, `System.loadLibrary` yok, ortak bellek yok.

Bunun iki sonucu var. Teknik olarak motor çökse uygulama çökmez. Lisans
açısından ise: FSF'in kendi GPL SSS'i, boru/soket üzerinden konuşan programları
"ayrı programlar" sayıyor — yani Kotlin kodu Stockfish'in türev çalışması
sayılmayabilir. Yine de proje bilinçli olarak **GPLv3 seçildi**: uygulamanın
satış argümanı "reklamsız ve seni takip etmiyor" ve buna inandırmanın en güçlü
yolu kodu açmak.

Kesin olan tek yükümlülük: dağıtılan binary'lerin karşılık gelen kaynağını
erişilebilir kılmak. Bu, değiştirilmemiş Stockfish 17.1 + `build-stockfish.sh`
ile karşılanıyor.

### Binary neden `lib*.so` adında?

O dosya bir kütüphane değil, **çalıştırılabilir bir program**. `lib*.so`
adlandırması, Android'in onu `nativeLibraryDir`'e çalıştırma izniyle
çıkarmasını sağlayan tek yol. `useLegacyPackaging = true` de sıkıştırılmadan
diske yazılmasını garanti ediyor — olmasa APK içinde sıkışık kalır ve
çalıştırılamazdı.

### Binary'ler neden git'te değil?

Her yeniden derleme git geçmişine kalıcı olarak ~150 MB ekliyordu; `.git`
255 MB'a çıkmıştı. Eylül 2026'da `git filter-repo` ile geçmişten temizlendi
(4 blob, ~303 MiB) ve `.git` 2.1 MB'a düştü. Aynı temizlikte eski kişisel
e-posta adresi de dosya içeriklerinden çıkarıldı.

**Kritik:** binary eksikken uygulama **çökmüyor** — `ChessAi.kt` sessizce çok
daha zayıf `BuiltInEngine`'e düşüyor. Yani temiz bir klondan hatasız, imzalı
ama motoru sakat bir yayın paketi üretilebilirdi. `build.gradle.kts` artık
release/bundle görevlerinde binary'leri doğruluyor ve eksikse duruyor.
x86_64 muaf: o ABI'de zaten binary yok, `BuiltInEngine` tasarım gereği.

### Varsayılan dil neden İngilizce?

`values/` İngilizce, `values-tr/` Türkçe. Başta tersiydi ve bu bir hataydı:
cihaz dili Türkçe veya İngilizce olmayan **herkes** (Alman, Fransız, Japon)
Türkçe arayüz görüyordu. Varsayılan, "hiçbir çeviri eşleşmezse ne gösterilir"
demek. Aynı mantıkla Play Console'un varsayılan dili de en-US, Türkçe ek dil.

`res/xml/locales_config.xml` sayesinde Android 13+'te kullanıcı cihaz dilinden
bağımsız olarak uygulama dilini seçebiliyor.

### Tahta neden sadece TV'de dönüyor?

İki kişilik modda sıra değişince tahtanın dönmesi TV'de mantıklı (herkes aynı
ekrana bakıyor, kumanda el değiştiriyor). Telefon ve tablette ise cihaz genelde
iki oyuncunun **arasına düz konuyor**, dönmesi kafa karıştırıyor. Bu yüzden
`GameViewModel`'deki otomatik dönüş `FEATURE_LEANBACK` kontrolüne bağlı.

### Zorluk seviyeleri neden derinlikle sınırlı?

Stockfish'in kendi "Skill Level" ayarı tek başına yeterince zayıflamıyor; alt
seviyelerde bile sıradan oyuncuyu eziyor. Bu yüzden 1-3. seviyelerde
`go depth 1..3` ile arama derinliği de sınırlanıyor. Amaç motorun **gerçekten
taş sarkıtması**. Varsayılan seviye Normal (3) — hiç yenilemeyen bir rakip
sıkıcı olduğu için bilinçli tercih.

---

## 5. Test cihazları

| Cihaz | Not |
|---|---|
| Samsung Galaxy S24 (`RFCY50RGFZB`) | arm64, 1080x2340 @480dpi = **360x780dp**. Emülatörlerden dar — telefon düzeni burada doğrulanmalı. |
| Smart TV (32-bit) | `armeabi-v7a`. adb üzerinden kablosuz; **IP değişiyor**, her seferinde bak. |
| Emülatör | x86_64 — burada Stockfish yok, `BuiltInEngine` çalışır. Motor davranışı buradan değerlendirilemez. |

UI değişikliklerini tahminle değil, **cihaza kurup ekran görüntüsü alarak**
doğrula. Bu projede iki kez tahminle ıskalandı.

```bash
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
adb exec-out screencap -p > ekran.png
```

---

## 6. Sık yapılan işler

**Debug derle ve telefona kur**
```bash
gradlew.bat :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

**Yayın paketi (AAB) üret**
```bash
gradlew.bat :app:bundleRelease
keytool -printcert -jarfile app/build/outputs/bundle/release/app-release.aab
```
İkinci komutun çıktısı boşsa paket imzasızdır — `keystore.properties` eksiktir.

**16 KB hizalamayı denetle**
```bash
bash tools/check-16kb.sh app/build/outputs/bundle/release/app-release.aab
```

**Motoru yeniden üret** (temiz klonda gerekir)
```bash
bash tools/build-stockfish.sh
```
Ya da GitHub Releases `v1.0` altındaki `.so` dosyalarını indirip
`app/src/main/jniLibs/<abi>/libstockfish.so` adıyla koy.

**Mağaza görsellerini yeniden üret**
```bash
python tools/make-store-graphics.py
```
Bu betik hem `docs/store/` altındaki mağaza görsellerini hem de uygulamanın
başlatıcı simgesini ve TV banner'ını üretir — ikisi ayrışmasın diye tek yerden.

---

## 7. Şu an ne durumda

**Bitmiş:**
- Uygulama telefon, tablet ve TV'de çalışıyor
- GPLv3, depo public, README iki dilli
- GitHub Pages yayında (tanıtım + gizlilik politikası)
- GitHub Releases `v1.0`: 3 APK + 2 motor binary'si + SHA256SUMS
- İmzalama anahtarı oluşturuldu ve yedeklendi
- İmzalı AAB hazır, 16 KB denetimi temiz
- Play Console'da **iç test** yayında

**Yapılmamış:**
- Kapalı test başlatılmadı → 12 kişi/14 gün saati **işlemiyor**
- Üretime çıkılmadı
- Destek ürünleri (`tip_small` / `tip_medium` / `tip_large`) Console'da
  tanımlanmadı — o yüzden destek ekranı "yüklenemedi" diyor, bu beklenen davranış
- Mağaza için Hakkında ekran görüntüsü eski (künye eklenmeden önce çekilmiş)
- Tablet ekran görüntüleri hiç yok — tabletlerde "telefon uygulaması" gibi listelenir

**Sonraki aday özellikler:** online mod, Lichess puzzle veritabanı.

---

## 8. Dokunurken dikkat

- **Paket adı** (`com.hilspot.chess`) ve **ücretsiz** kararı geri alınamaz
- `versionCode` her Play yüklemesinde artmalı, aynısı ikinci kez kabul edilmiyor
- `SupportViewModel.PRODUCT_IDS` ile Console'daki ürün kimlikleri **birebir**
  aynı olmalı, yoksa ekran sessizce "yüklenemedi" der
- `keystore.properties`, `*.jks`, `.claude/settings*.json` ve motor binary'leri
  gitignore'da — depo public, yanlışlıkla eklemeyin
- Play App Signing açılırsa Play sürümü Google'ın anahtarıyla imzalanır;
  GitHub'daki APK ile **birbirinin üzerine güncellenemez**
