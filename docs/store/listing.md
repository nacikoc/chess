# Play Console — mağaza kaydı metinleri

Karakter sınırları: uygulama adı 30, kısa açıklama 80, tam açıklama 4000.
Aşağıdaki metinler bu sınırların içinde.

---

## Türkçe (tr-TR) — varsayılan dil

**Uygulama adı** (17/30)

```
Satranç — Reklamsız
```

**Kısa açıklama** (79/80)

```
Reklamsız, çevrimdışı satranç. Telefon, tablet ve TV'de. Güçlü motor, 8 seviye.
```

**Tam açıklama**

```
Satranç oynamak isteyip de reklam bombardımanına tutulmaktan sıkıldıysanız bu
uygulama tam size göre. Reklam yok, abonelik yok, kilitli özellik yok, hesap
açmak yok. Kurun ve oynayın.

TAMAMEN ÇEVRİMDIŞI
Yapay zekâ rakip cihazınızda çalışır. İnternet bağlantısı gerekmez — uçakta,
metroda, dağ başında aynı şekilde oynar. Uygulama sizin hakkınızda hiçbir veri
toplamaz.

GÜÇLÜ AMA YENİLEBİLİR RAKİP
Dünyanın en güçlü satranç motorlarından Stockfish kullanılıyor. 8 zorluk
seviyesi var ve alt seviyeler gerçekten yenilebilir olacak şekilde ayarlandı:
Acemi ve Kolay seviyelerde motor kasıtlı olarak hata yapar, taş sarkıtır.
Yukarı çıktıkça acımasızlaşır. Yeni başlayan da, iddialı oyuncu da kendine
uygun bir rakip bulur.

• Acemi · Kolay · Normal · Orta · İyi · Zor · Usta · Büyükusta

İKİ KİŞİLİK OYUN
Aynı cihazda karşılıklı oynayın. Telefonu ya da tableti aranıza koyun; TV'de
oynarken tahta her hamlede sırası gelen oyuncuya döner ve imleç onun tarafına
atlar.

TELEFON, TABLET VE ANDROID TV
Tek uygulama üçünde de çalışır. Telefonda tahta ve kontroller tek elle
oynayabileceğiniz şekilde alta yerleştirildi. TV'de kumandayla rahatça
gezinmeniz için belirgin bir odak göstergesi var.

OYUN SIRASINDA
• Alınan taşlar ve materyal farkı
• Hamle listesi ve geri alma
• Şah, mat, pat ve beraberlik tespiti
• Terfi ederken taş seçimi
• Tahtayı çevirme

AÇIK KAYNAK
Uygulama özgür yazılımdır ve GNU GPL v3 ile lisanslanmıştır. Kaynak kodun
tamamı herkese açık: github.com/nacikoc/chess
İçinde reklam ya da takip kodu olmadığını iddia etmiyoruz — kodu açıp
kendiniz görebilirsiniz.

DESTEK
Uygulama ücretsiz ve öyle kalacak. Beğenirseniz uygulama içinden isteğe bağlı
bir bağış bırakabilirsiniz. Bağış hiçbir özelliği açmaz; her şey zaten
herkese açıktır.
```

---

## English (en-US)

**App name** (16/30)

```
Chess — No Ads
```

**Short description** (76/80)

```
Ad-free, offline chess for phone, tablet and TV. Strong engine, 8 levels.
```

**Full description**

```
If you just want to play chess without being buried in ads, this is for you.
No ads, no subscription, no locked features, no account. Install and play.

FULLY OFFLINE
The computer opponent runs on your device. No internet connection needed — it
works the same on a plane, on the metro or off the grid. The app collects no
data about you.

STRONG, BUT BEATABLE
Powered by Stockfish, one of the strongest chess engines in the world. Eight
difficulty levels, with the lower ones deliberately tuned to be beatable: at
Beginner and Easy the engine really does blunder and hang pieces. It gets
ruthless as you climb. Whether you are learning or looking for a fight, there
is a level for you.

• Beginner · Easy · Normal · Medium · Good · Hard · Master · Grandmaster

TWO PLAYERS
Play face to face on one device. Put the phone or tablet between you; on TV the
board turns to whoever is to move and the cursor jumps to their side.

PHONE, TABLET AND ANDROID TV
One app for all three. On the phone the board and controls sit low so you can
play one-handed. On TV a clear focus indicator makes remote navigation easy.

DURING THE GAME
• Captured pieces and material advantage
• Move list and undo
• Check, checkmate, stalemate and draw detection
• Promotion piece choice
• Flip the board

OPEN SOURCE
This app is free software, licensed under the GNU GPL v3. The full source code
is public: github.com/nacikoc/chess
We do not just claim there are no ads or trackers — you can read the code.

SUPPORT
The app is free and will stay free. If you like it you can leave an optional
tip from inside the app. A tip unlocks nothing; everything is already
available to everyone.
```

---

## Diğer Console alanları

| Alan | Değer |
|---|---|
| Kategori | Oyunlar → Tahta oyunu (Board) |
| Etiketler | satranç, tahta oyunu, çevrimdışı, reklamsız |
| İletişim e-postası | nakisoft@gmail.com |
| Gizlilik politikası | https://nacikoc.github.io/chess/privacy.html |
| Reklam içeriyor mu | Hayır |
| Uygulama içi satın alma | Evet (isteğe bağlı bağış) |
| İçerik derecelendirmesi | Herkes / 3+ |

## Grafik varlıklar

| Dosya | Nerede kullanılır |
|---|---|
| `icon-512.png` | Uygulama simgesi (512×512) |
| `feature-1024x500.png` | Öne çıkan grafik |
| `tv-banner-1280x720.png` | Android TV banner |
| `screenshots/phone-*.png` | Telefon ekran görüntüleri (1080×2112) |
| `screenshots/tv-*.png` | Android TV ekran görüntüleri (1920×1080) |

Telefon görüntülerinden sistem çubukları kırpıldı: hem daha temiz görünüyor
hem de Play'in "uzun kenar kısa kenarın 2 katını geçemez" kuralına giriyor
(ham 1080×2340 bu kuralı ihlal ediyordu).

Grafikleri yeniden üretmek için: `python tools/make-store-graphics.py`
