# Play Console — Veri güvenliği formu cevapları

Play Console → Uygulama içeriği → Veri güvenliği. Aşağıdakiler bu uygulamanın
doğru cevaplarıdır. **Destek özelliği eklendikten sonra bile değişmez**, çünkü
ödemeyi Google Play yürütür ve geliştirici hiçbir kişisel veriye erişmez.

## Veri toplama

| Soru | Cevap |
|---|---|
| Uygulamanız kullanıcı verisi topluyor veya paylaşıyor mu? | **Hayır** |

Bu "Hayır" cevabıyla birlikte veri türü listelerinin hiçbirini işaretlemene
gerek kalmaz. Gerekçe:

- Sunucu yok, hesap yok, analitik SDK'sı yok, reklam SDK'sı yok, çökme
  raporlama SDK'sı yok.
- Oyun durumu yalnızca bellekte tutulur, diske bile yazılmaz.
- Google Play Billing üzerinden yapılan destek ödemesinde veriyi **Google**
  toplar. Play'in veri güvenliği rehberine göre, Google Play'in kendi ödeme
  altyapısının işlediği veriler geliştiricinin beyanına dahil edilmez.

## Güvenlik uygulamaları

| Soru | Cevap |
|---|---|
| Veriler aktarımda şifreleniyor mu? | Veri toplanmadığı için geçerli değil (form "Hayır" dediğinde sormaz) |
| Kullanıcı veri silinmesini isteyebiliyor mu? | Geçerli değil — saklanan veri yok |

## İlgili diğer beyanlar

**Reklamlar:** Uygulama reklam içermiyor → "Uygulamanız reklam içeriyor mu?"
sorusuna **Hayır**.

**Uygulama içi satın alma:** Destek ürünleri eklendiğinde Play bunu otomatik
algılar; mağaza sayfasında "Uygulama içi satın alma" etiketi görünür. Bu
beklenen durumdur — destekler isteğe bağlıdır ve hiçbir özellik açmaz.
Açıklama metninde bunun açıkça yazılması, kullanıcı şikâyetini önler.

**Hedef kitle ve içerik:** Her yaşa uygun. Çocuklara özel olarak hedeflenmiyor
ancak çocuklar için de güvenli (veri toplanmıyor, reklam yok, dış bağlantı
yalnızca Hakkında ekranındaki kaynak kod adresi).

**İçerik derecelendirmesi:** Şiddet, korku, kumar, kullanıcı etkileşimi yok.
Anket "Herkes / 3+" ile sonuçlanır. **Ancak IARC anketindeki "uygulama dijital
satın alma sunuyor mu?" sorusuna EVET denmeli** — destek ürünleri birer
uygulama içi satın almadır. Bunu atlarsan derecelendirme yanlış beyan sayılır;
cevap derecelendirmeyi 3+'tan çıkarmaz, yalnızca mağaza sayfasına "Uygulama içi
satın alma" etiketi ekler.

**Devlet uygulaması / finans uygulaması:** Hayır.

## Gizlilik politikası URL'i

Depo herkese açıldıktan ve GitHub Pages (Settings → Pages → Branch: `main`,
klasör: `/docs`) etkinleştirildikten sonra:

```
https://nacikoc.github.io/chess/privacy.html
```

Bu adresi Play Console'da hem **Uygulama içeriği → Gizlilik politikası**
alanına hem de mağaza kaydına gir.
