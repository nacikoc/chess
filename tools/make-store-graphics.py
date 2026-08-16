#!/usr/bin/env python3
"""Play Store gorsellerini uretir (ikon, one cikan gorsel, TV banner).

Uygulamanin kendi paletini ve paketli DejaVu yazi tipini kullanir, boylece
magaza gorselleri uygulamanin icindekiyle ayni dile sahip olur.

Kullanim:  python tools/make-store-graphics.py
Cikti:     docs/store/
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FONT = ROOT / "app/src/main/res/font/chess_font.ttf"
OUT = ROOT / "docs/store"
OUT.mkdir(parents=True, exist_ok=True)

GREEN_DARK = (20, 38, 29)
GREEN = (30, 64, 50)
GREEN_LIT = (46, 92, 70)
INK = (17, 16, 12)
GOLD = (232, 200, 136)
CREAM = (237, 230, 218)
MUTED = (154, 145, 132)
KNIGHT = "♞"  # ♞


def font(size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONT), size)


def centered(draw, xy, text, fnt, fill):
    """Metni glif kutusuna gore gercekten ortalar (baseline'a gore degil)."""
    x, y = xy
    l, t, r, b = draw.textbbox((0, 0), text, font=fnt)
    draw.text((x - (r + l) / 2, y - (b + t) / 2), text, font=fnt, fill=fill)


def vertical_gradient(size, top, bottom):
    w, h = size
    img = Image.new("RGB", (1, h))
    px = img.load()
    for y in range(h):
        f = y / max(1, h - 1)
        px[0, y] = tuple(round(top[i] + (bottom[i] - top[i]) * f) for i in range(3))
    return img.resize((w, h))


def radial_disc(diameter, inner, outer):
    """Yumusak radyal gecisli daire (RGBA)."""
    img = Image.new("RGBA", (diameter, diameter), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    steps = 48
    for i in range(steps, 0, -1):
        f = i / steps
        r = diameter / 2 * f
        col = tuple(round(outer[j] + (inner[j] - outer[j]) * (1 - f)) for j in range(3))
        c = diameter / 2
        d.ellipse([c - r, c - r, c + r, c + r], fill=col + (255,))
    return img


def make_icon():
    """512x512, tam dolu kare (Play ikonu seffaflik kabul etmez)."""
    s = 512
    img = Image.new("RGB", (s, s), GREEN)
    img.paste(radial_disc(s, GREEN_LIT, GREEN_DARK).convert("RGB"), (0, 0))
    d = ImageDraw.Draw(img)
    centered(d, (s / 2, s / 2 + 8), KNIGHT, font(330), GOLD)
    img.save(OUT / "icon-512.png")
    return "icon-512.png"


def make_feature():
    """1024x500 one cikan gorsel."""
    w, h = 1024, 500
    img = vertical_gradient((w, h), GREEN_DARK, INK)
    d = ImageDraw.Draw(img)

    # Sag ust kosede soluk satranc deseni
    cell = 46
    for row in range(3):
        for col in range(6):
            if (row + col) % 2 == 0:
                continue
            x = w - 6 * cell + col * cell
            y = row * cell - 10
            d.rectangle([x, y, x + cell, y + cell], fill=(28, 46, 36))

    disc = 200
    img.paste(radial_disc(disc, GREEN_LIT, GREEN), (72, (h - disc) // 2),
              radial_disc(disc, GREEN_LIT, GREEN))
    d.ellipse([72, (h - disc) // 2, 72 + disc, (h - disc) // 2 + disc],
              outline=GOLD, width=3)
    centered(d, (72 + disc / 2, h / 2 + 6), KNIGHT, font(130), GOLD)

    d.text((320, 176), "Satranç", font=font(96), fill=CREAM)
    d.text((326, 292), "Reklamsız · Tamamen çevrimdışı", font=font(34), fill=MUTED)
    d.text((326, 340), "Telefon · Tablet · Android TV", font=font(28), fill=(120, 112, 100))
    img.save(OUT / "feature-1024x500.png")
    return "feature-1024x500.png"


def make_tv_banner():
    """1280x720 Android TV banner."""
    w, h = 1280, 720
    img = vertical_gradient((w, h), GREEN_DARK, INK)
    d = ImageDraw.Draw(img)

    disc = 300
    cx = w / 2
    img.paste(radial_disc(disc, GREEN_LIT, GREEN), (int(cx - disc / 2), 120),
              radial_disc(disc, GREEN_LIT, GREEN))
    d.ellipse([cx - disc / 2, 120, cx + disc / 2, 120 + disc], outline=GOLD, width=4)
    centered(d, (cx, 120 + disc / 2 + 8), KNIGHT, font(200), GOLD)

    centered(d, (cx, 500), "Satranç", font(104), CREAM)
    centered(d, (cx, 580), "Reklamsız · Tamamen çevrimdışı", font(38), MUTED)
    img.save(OUT / "tv-banner-1280x720.png")
    return "tv-banner-1280x720.png"


if __name__ == "__main__":
    for name in (make_icon(), make_feature(), make_tv_banner()):
        print("uretildi:", (OUT / name).relative_to(ROOT))
