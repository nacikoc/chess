# Play Console'a yükleme — adım adım

Bu dosya, uygulamanın **ilk kez** Google Play'e çıkarılması için Console'da
elle yapılacak işleri sırasıyla anlatır. Derleme tarafı için
[RELEASING.md](../RELEASING.md), form cevapları için
[play-data-safety.md](play-data-safety.md), mağaza metinleri için
[store/listing.md](store/listing.md).

| | |
|---|---|
| Paket adı | `com.hilspot.chess` |
| Geliştirici | Naci Koç |
| İletişim | nakisoft@gmail.com |
| Fiyat | Ücretsiz + isteğe bağlı uygulama içi destek |

---

## 0. Geri dönüşü olmayan kararlar

Bunları yanlış girersen düzeltmenin tek yolu sıfırdan yeni bir uygulama
oluşturmaktır:

- **Paket adı** (`com.hilspot.chess`) — ilk yüklemeden sonra değişmez.
- **Ücretsiz/Ücretli** — ücretsiz seçilen bir uygulama sonradan ücretliye
  çevrilemez (tersi mümkündür).
- **İmzalama anahtarı** — kaybedersen uygulamayı bir daha güncelleyemezsin.
  Play App Signing'i açarsan yalnızca *yükleme* anahtarını kaybetme riski
  kalır, o da Google'a başvurarak değiştirilebilir. **Aç.**
- **Varsayılan dil** — sonradan değiştirilebilir ama tüm çevirileri etkiler.
  **İngilizce (en-US) seç**, sonra Türkçe'yi ek dil olarak ekle. Varsayılan
  dil, kendi dilinde çeviri bulunmayan herkesin gördüğü metindir: İngilizce
  seçersen Alman da Japon da anlayabileceği bir sayfa görür, Türk kullanıcı
  yine Türkçe görür. Türkçe seçersen tersi olur ve Türkiye dışındaki herkes
  anlamadığı bir sayfayla karşılaşır. Uygulamanın içi de aynı mantıkla
  kuruldu (`values/` İngilizce, `values-tr/` Türkçe).

---

## 1. Geliştirici hesabı

1. [play.google.com/console](https://play.google.com/console) → hesap aç.
2. **Hesap türü: Bireysel (Personal).** 25 USD tek seferlik ücret.
3. Kimlik doğrulama: bireysel hesapta resmî kimlik + adres istenir. D-U-N-S
   numarası yalnızca kurumsal hesaplarda gerekir, sende gerekmeyecek.
4. **Geliştirici adı: `Naci Koç`.** Bu ad mağazada uygulamanın altında
   görünür — kullanıcının "kim yapmış" diye baktığında göreceği yer burasıdır.
5. İletişim e-postası: `nakisoft@gmail.com`. Bu adres mağaza sayfasında
   herkese açık görünür.
6. **Ödeme profili (payments profile)** oluştur — uygulama ücretsiz olsa bile
   uygulama içi destek ürünleri için zorunlu. Vergi bilgilerini de burada
   girersin.

> **Yeni bireysel hesaplarda kapalı test şartı:** Kasım 2023'ten sonra açılan
> bireysel hesaplar, üretime (production) çıkmadan önce en az **12 test
> kullanıcısıyla, 14 gün kesintisiz** bir kapalı test yürütmek zorunda. Bu
> kural zaman zaman güncelleniyor; Console sana kendi ekranında geçerli şartı
> gösterir — asıl kaynak orasıdır. Pratik anlamı: **üretim yayınından ~2 hafta
> önce başlaman gerekir** ve 12 kişi (Google hesabı olan tanıdıklar yeter)
> bulman lazım.

---

## 2. İmzalama anahtarı

[RELEASING.md § 1](../RELEASING.md) adımlarını uygula. Özetle:

```bash
keytool -genkeypair -v -keystore chess-release.jks -keyalg RSA -keysize 4096 -validity 10000 -alias chess
```

Sonra proje kökünde `keystore.properties` oluştur (`.gitignore`'da, depoya
girmez). Anahtarı ve parolasını **iki ayrı yerde** yedekle.

---

## 3. Uygulamayı oluştur

Console → **Uygulama oluştur**:

| Alan | Değer |
|---|---|
| Uygulama adı | `Satranç — Çevrimdışı` (başlıkta "Reklamsız" yasak — bkz. [listing.md](store/listing.md)) |
| Varsayılan dil | **İngilizce (en-US)** — sonra Türkçe'yi ek dil olarak gir |
| Uygulama mı, oyun mu | **Oyun** |
| Ücretsiz mi, ücretli mi | **Ücretsiz** |
| Beyanlar | Geliştirici Programı Politikaları + ABD ihracat yasaları → işaretle |

---

## 4. AAB üret ve yükle

```bash
gradlew.bat :app:bundleRelease
```

Çıktı: `app/build/outputs/bundle/release/app-release.aab`

İmzalı olduğunu doğrula (çıktı boşsa imzasızdır, Play reddeder):

```bash
keytool -printcert -jarfile app/build/outputs/bundle/release/app-release.aab
```

Yükleme yeri: **Test → Kapalı test → Sürüm oluştur** (üretime doğrudan değil,
madde 1'deki kapalı test şartı yüzünden).

---

## 5. "Uygulama içeriği" formları

Hepsi zorunlu; biri eksikse yayın başlatılamaz.

| Form | Cevap |
|---|---|
| Gizlilik politikası | `https://nacikoc.github.io/chess/privacy.html` |
| Reklamlar | **Hayır**, uygulama reklam içermiyor |
| Uygulama erişimi | Tüm işlevler kısıtlama olmadan kullanılabilir |
| İçerik derecelendirmesi | Anketi doldur → "Herkes / 3+" çıkar. "Dijital satın alma sunuyor mu?" → **Evet** |
| Hedef kitle | 13+ (çocuklara özel hedeflenmiyor) |
| Veri güvenliği | [play-data-safety.md](play-data-safety.md) — "veri toplanmıyor" |
| Devlet uygulaması | Hayır |
| Finans özellikleri | Hayır |
| Sağlık uygulaması | Hayır |

> Gizlilik politikası URL'inin **yayında** olması gerekir. Depoyu herkese
> açtıktan sonra GitHub → Settings → Pages → Branch `main`, klasör `/docs`.
> Adresi tarayıcıda açıp çalıştığını gör, sonra Console'a gir. Sitenin kökü
> (`nacikoc.github.io/chess/`) [docs/index.html](index.html) sayesinde boş
> 404 vermez; oradan gizlilik politikasına ve kaynak koda bağlantı var.

---

## 6. Mağaza kaydı

Metinler: [store/listing.md](store/listing.md) (Türkçe + İngilizce hazır).

| Varlık | Boyut | Dosya |
|---|---|---|
| Uygulama simgesi | 512×512 | `store/icon-512.png` |
| Öne çıkan grafik | 1024×500 | `store/feature-1024x500.png` |
| Telefon ekran görüntüleri | 1080×2112 (en az 2, en fazla 8) | `store/screenshots/phone-*.png` |
| Android TV banner | 1280×720 | `store/tv-banner-1280x720.png` |
| Android TV ekran görüntüleri | 1920×1080 (en az 2) | `store/screenshots/tv-*.png` |

---

## 7. Cihaz kategorileri — TV ve tablet

Uygulama üç form faktöründe de çalışıyor ama Play bunu kendiliğinden ilan
etmez:

- **Android TV:** Console → Sürümler → **Gelişmiş ayarlar → Form faktörleri →
  Android TV ekle**. TV banner'ı ve en az 2 TV ekran görüntüsü yüklenmeden bu
  bölüm onaylanmaz. Manifest'teki leanback beyanı zaten var.
- **Tablet:** tablet ekran görüntüsü yüklemezsen uygulama tabletlerde
  "telefon uygulaması" gibi listelenir ve tablet aramalarında geri plana
  düşer. 7 inç ve 10 inç için ayrı ekran görüntüsü isteniyor —
  **bunlar depoda henüz yok**, tabletten alıp `docs/store/screenshots/`
  altına eklemek gerekiyor.

---

## 8. Uygulama içi destek ürünleri

Console → **Para kazanma → Ürünler → Uygulama içi ürünler**. Üçünü de
**tüketilebilir (consumable)** olarak oluştur; ürün kimlikleri kodda sabit:

| Ürün kimliği | Öneri |
|---|---|
| `tip_small` | ~₺30 |
| `tip_medium` | ~₺75 |
| `tip_large` | ~₺150 |

Kimlikler `SupportViewModel.kt` içindeki `PRODUCT_IDS` ile **birebir** aynı
olmalı; yanlış yazılırsa ekran "yüklenemedi" der.

Ürünler tanımlanana kadar uygulama çökmez, ekranda "şu anda yüklenemedi"
mesajı görünür — bu tasarlanmış davranış.

> **Kelime seçimi önemli:** Play'de "bağış / donation", vergiden muaf hayır
> amaçlı ödemeleri anlatır; o kategori kayıtlı hayır kurumlarına ayrılmıştır
> ve ayrı bir doğrulama ister. Buradaki ödeme bir kişisel geliştirici
> desteğidir, hiçbir içerik açmaz. Uygulama içi metinler ve bu depodaki
> belgeler bu yüzden "destek/tip" diliyle yazıldı — **Console'da ürün adlarını
> girerken de "bağış/donation" deme**, "Küçük destek / Orta destek / Büyük
> destek" gibi yaz.

---

## 9. Kapalı test → üretim

1. Kapalı test sürümünü yayınla.
2. **12 test kullanıcısını** e-posta listesiyle ekle; hepsinin bağlantıyı
   açıp uygulamayı **kurması** gerekir.
3. 14 gün kesintisiz bekle (birileri çıkarsa sayaç sıfırlanır).
4. Console "üretime başvurabilirsin" der → **Üretim → Sürüm oluştur**, aynı
   AAB'yi taşı.
5. İnceleme birkaç saatten birkaç güne kadar sürebilir; ilk yayında genelde
   daha uzun.

---

## 10. Yayın sonrası

- `versionCode`'u her güncellemede artır (Play aynısını ikinci kez kabul
  etmez).
- Kaynak kod deposu herkese açık kalmalı — GPLv3 yükümlülüğü, Play'in değil
  lisansın şartı.
- Mağaza sayfasındaki iletişim adresine (`nakisoft@gmail.com`) gelen
  kullanıcı e-postalarını yanıtsız bırakma; Play bunu kalite sinyali sayıyor.
