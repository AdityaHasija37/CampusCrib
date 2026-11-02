"""Generates the original flat illustrations for CampusCrib's demo listings (no stock imagery).
Each apartment gets an exterior, living room, bedroom and kitchen in its own colours.

Usage: python3 scripts/draw_listing_illustrations.py src/main/resources/seed/apartments  (needs Pillow)
"""
import random, sys, os
from PIL import Image, ImageDraw, ImageFilter

W, H, S = 1200, 750, 2  # draw at 2x, downsample for smooth edges
OUT = sys.argv[1]

APTS = {
    # code: (facade, accent, wall, floor, sofa/bed, sky, storeys, style)
    "A101": ("#C8654A", "#2F5D50", "#F1E6D8", "#B98B62", "#2F5D50", ("#F7D9C4", "#FBEFE4"), 4, "brick"),
    "A102": ("#E7DCCB", "#B5482A", "#EDE3D3", "#C9A27A", "#B5482A", ("#D7E6EE", "#F4F1EA"), 3, "townhouse"),
    "B201": ("#9C5B3F", "#2A2522", "#F3E9DC", "#A9764F", "#3E5A73", ("#F5E1C8", "#FBF3EA"), 4, "brownstone"),
    "C301": ("#5E7F72", "#E2A23B", "#EFE7DA", "#B88F63", "#C8654A", ("#DDEBE3", "#F6F3EC"), 3, "triple"),
    "D401": ("#3E5A73", "#E58A6C", "#ECE6DD", "#9F7B58", "#E2A23B", ("#DCE3EF", "#F3F1EC"), 6, "tower"),
}

def P(*v):
    return tuple(int(x * S) for x in v)

def hexc(h, a=255):
    h = h.lstrip("#"); return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)

def shade(h, f):
    r, g, b, _ = hexc(h)
    return (max(0, min(255, int(r * f))), max(0, min(255, int(g * f))), max(0, min(255, int(b * f))), 255)

def gradient(img, top, bottom, box):
    x0, y0, x1, y1 = box
    d = ImageDraw.Draw(img)
    t, b = hexc(top), hexc(bottom)
    for y in range(y0, y1):
        k = (y - y0) / max(1, y1 - y0)
        d.line([(x0, y), (x1, y)], fill=tuple(int(t[i] + (b[i] - t[i]) * k) for i in range(3)) + (255,))

def canvas():
    return Image.new("RGBA", (W * S, H * S), (255, 255, 255, 255))

def finish(img, path):
    img = img.resize((W, H), Image.LANCZOS).convert("RGB")
    img.save(path, quality=86)

def tree(d, x, base, h, color="#2F5D50"):
    d.rectangle(P(x - 5, base - h * 0.35, x + 5, base), fill=hexc("#6B4A33"))
    for i, r in enumerate([h * 0.42, h * 0.34, h * 0.26]):
        cy = base - h * 0.45 - i * h * 0.18
        d.ellipse(P(x - r, cy - r, x + r, cy + r), fill=shade(color, 1 + i * 0.08))

def exterior(code, spec, path):
    facade, accent, wall, floor, soft, sky, storeys, style = spec
    rnd = random.Random(code)
    img = canvas(); d = ImageDraw.Draw(img)
    gradient(img, sky[0], sky[1], P(0, 0, W, H))
    # sun and clouds
    d.ellipse(P(930, 90, 1030, 190), fill=hexc("#F6C98E"))
    for cx, cy in [(220, 120), (640, 80), (1050, 260)]:
        for dx, r in [(-40, 28), (0, 38), (42, 26)]:
            d.ellipse(P(cx + dx - r, cy - r, cx + dx + r, cy + r), fill=(255, 255, 255, 235))
    ground = 640
    d.rectangle(P(0, ground, W, H), fill=hexc("#C9D7B8"))
    d.rectangle(P(0, ground + 40, W, H), fill=hexc("#D9CFC0"))
    # neighbour buildings for depth
    for x, w, h in [(40, 220, 300), (930, 230, 360)]:
        d.rectangle(P(x, ground - h, x + w, ground), fill=shade(facade, 0.82) if style != "tower" else hexc("#AEB9C6"))
    floors = storeys
    bw = {"tower": 420, "townhouse": 460, "triple": 500}.get(style, 520)
    fh = {"tower": 72, "townhouse": 120}.get(style, 100)
    bx = (W - bw) // 2; top = ground - floors * fh - 30
    # roof
    if style in ("townhouse", "triple"):
        d.polygon(P(bx - 20, top + 10, bx + bw / 2, top - 90, bx + bw + 20, top + 10), fill=shade(facade, 0.7))
    else:
        d.rectangle(P(bx - 12, top - 16, bx + bw + 12, top + 6), fill=shade(facade, 0.7))
    d.rectangle(P(bx, top, bx + bw, ground), fill=hexc(facade))
    if style in ("brick", "brownstone"):
        for y in range(int(top), ground, 14):
            off = 0 if (y // 14) % 2 else 22
            for x in range(bx + off, bx + bw, 44):
                d.line(P(x, y, x, y + 14), fill=shade(facade, 0.9), width=S)
            d.line(P(bx, y, bx + bw, y), fill=shade(facade, 0.9), width=S)
    cols = {"tower": 5, "townhouse": 3}.get(style, 4)
    ww, wh = bw / cols * 0.5, fh * 0.55
    for f in range(floors):
        y = top + 20 + f * fh
        for c in range(cols):
            x = bx + (c + 0.25) * bw / cols
            lit = rnd.random() < 0.35
            d.rectangle(P(x - 6, y - 6, x + ww + 6, y + wh + 6), fill=shade(facade, 0.72))
            d.rectangle(P(x, y, x + ww, y + wh), fill=hexc("#F7D38A") if lit else hexc("#9CC3D8"))
            d.line(P(x + ww / 2, y, x + ww / 2, y + wh), fill=shade(facade, 0.72), width=3 * S)
            if style in ("tower", "brick") and f % 2 == 1:
                d.rectangle(P(x - 12, y + wh, x + ww + 12, y + wh + 10), fill=hexc(accent))
    # door and steps
    dx = bx + bw / 2 - 34
    d.rectangle(P(dx - 10, ground - 120, dx + 78, ground), fill=shade(facade, 0.68))
    d.rectangle(P(dx, ground - 110, dx + 68, ground), fill=hexc(accent))
    d.ellipse(P(dx + 52, ground - 60, dx + 60, ground - 52), fill=hexc("#F6C98E"))
    for i in range(3):
        d.rectangle(P(dx - 24 - i * 10, ground + i * 12, dx + 92 + i * 10, ground + 12 + i * 12), fill=hexc("#CDBFAE"))
    # unit sign
    d.rounded_rectangle(P(bx + 20, ground - 70, bx + 120, ground - 34), radius=8 * S, fill=(255, 255, 255, 255))
    tree(d, bx - 70, ground + 20, 230); tree(d, bx + bw + 80, ground + 20, 260, "#3E6E5F"); tree(d, 160, ground + 30, 170, "#4C7A62")
    finish(img, path)

def window(d, x, y, w, h, sky=("#BFD8E6", "#E9F2F4")):
    d.rectangle(P(x - 10, y - 10, x + w + 10, y + h + 10), fill=hexc("#FFFFFF"))
    d.rectangle(P(x, y, x + w, y + h), fill=hexc(sky[0]))
    d.rectangle(P(x, y + h * 0.6, x + w, y + h), fill=hexc("#CFE1D4"))
    d.line(P(x + w / 2, y, x + w / 2, y + h), fill=hexc("#FFFFFF"), width=6 * S)
    d.line(P(x, y + h / 2, x + w, y + h / 2), fill=hexc("#FFFFFF"), width=6 * S)

def plant(d, x, base, color="#2F5D50"):
    d.polygon(P(x - 26, base - 60, x + 26, base - 60, x + 18, base, x - 18, base), fill=hexc("#C8654A"))
    for ang in [(-50, -170), (0, -210), (50, -170), (-25, -140), (25, -150)]:
        d.ellipse(P(x + ang[0] - 22, base - 60 + ang[1] * 0.6 - 40, x + ang[0] + 22, base - 60 + ang[1] * 0.6 + 40), fill=hexc(color))

def room_base(spec):
    facade, accent, wall, floor, soft, sky, *_ = spec
    img = canvas(); d = ImageDraw.Draw(img)
    gradient(img, wall, "#FFFFFF", P(0, 0, W, 520))
    d.rectangle(P(0, 520, W, H), fill=hexc(floor))
    for x in range(0, W, 120):
        d.line(P(x, 520, x - 160, H), fill=shade(floor, 0.92), width=2 * S)
    d.rectangle(P(0, 512, W, 524), fill=hexc("#FFFFFF"))
    return img, d

def living(code, spec, path):
    facade, accent, wall, floor, soft, *_ = spec
    img, d = room_base(spec)
    window(d, 120, 110, 300, 300)
    d.rounded_rectangle(P(280, 560, 940, 690), radius=60 * S, fill=shade(accent, 1.15) if accent != "#2A2522" else hexc("#D9C7B0"))  # rug
    # sofa
    d.rounded_rectangle(P(470, 340, 1050, 470), radius=30 * S, fill=shade(soft, 0.85))
    d.rounded_rectangle(P(450, 420, 1070, 560), radius=30 * S, fill=hexc(soft))
    for x in (520, 700, 880):
        d.rounded_rectangle(P(x, 380, x + 150, 470), radius=24 * S, fill=shade(soft, 1.12))
    d.rectangle(P(480, 560, 500, 590), fill=hexc("#4A3B30")); d.rectangle(P(1020, 560, 1040, 590), fill=hexc("#4A3B30"))
    # coffee table and lamp
    d.rounded_rectangle(P(560, 600, 860, 630), radius=10 * S, fill=hexc("#6B4A33"))
    d.ellipse(P(640, 570, 700, 600), fill=hexc("#F6C98E"))
    d.line(P(1110, 560, 1110, 260), fill=hexc("#2A2522"), width=6 * S)
    d.polygon(P(1060, 260, 1160, 260, 1135, 190, 1085, 190), fill=hexc("#F6C98E"))
    # wall art
    d.rectangle(P(620, 150, 900, 300), fill=hexc("#FFFFFF"))
    d.rectangle(P(640, 170, 880, 280), fill=hexc(facade))
    d.ellipse(P(780, 190, 840, 250), fill=hexc("#F6C98E"))
    plant(d, 90, 640)
    finish(img, path)

def bedroom(code, spec, path):
    facade, accent, wall, floor, soft, *_ = spec
    img, d = room_base(spec)
    window(d, 820, 120, 260, 280)
    d.rounded_rectangle(P(180, 230, 720, 470), radius=30 * S, fill=shade(soft, 0.8))  # headboard
    d.rectangle(P(160, 440, 740, 600), fill=hexc("#FFFFFF"))
    d.rectangle(P(160, 500, 740, 640), fill=hexc(soft))
    d.polygon(P(160, 500, 740, 500, 740, 540, 160, 520), fill=shade(soft, 1.15))
    for x in (210, 460):
        d.rounded_rectangle(P(x, 400, x + 210, 470), radius=26 * S, fill=hexc("#F7F2EA"))
    d.rectangle(P(170, 640, 190, 670), fill=hexc("#4A3B30")); d.rectangle(P(710, 640, 730, 670), fill=hexc("#4A3B30"))
    # nightstand and lamp, desk
    d.rectangle(P(40, 470, 140, 600), fill=hexc("#8A6447"))
    d.line(P(90, 470, 90, 400), fill=hexc("#2A2522"), width=4 * S)
    d.polygon(P(60, 400, 120, 400, 108, 350, 72, 350), fill=hexc("#F6C98E"))
    d.rectangle(P(820, 470, 1120, 490), fill=hexc("#8A6447"))
    d.rectangle(P(840, 490, 856, 640), fill=hexc("#8A6447")); d.rectangle(P(1084, 490, 1100, 640), fill=hexc("#8A6447"))
    d.rectangle(P(900, 400, 1020, 470), fill=hexc("#2A2522")); d.rectangle(P(908, 408, 1012, 462), fill=hexc("#9CC3D8"))
    d.rounded_rectangle(P(930, 520, 1030, 640), radius=18 * S, fill=hexc(accent))
    finish(img, path)

def kitchen(code, spec, path):
    facade, accent, wall, floor, soft, *_ = spec
    img, d = room_base(spec)
    # upper cabinets and tiles
    for x in range(80, 1120, 20):
        d.line(P(x, 300, x, 430), fill=shade(wall, 0.95), width=S)
    for y in range(300, 430, 20):
        d.line(P(80, y, 1120, y), fill=shade(wall, 0.95), width=S)
    for x in range(80, 700, 155):
        d.rectangle(P(x, 110, x + 145, 280), fill=hexc(facade)); d.rectangle(P(x + 60, 250, x + 85, 258), fill=hexc("#F6C98E"))
    window(d, 760, 120, 340, 230)
    # counter with sink and hob
    d.rectangle(P(60, 430, 1140, 460), fill=hexc("#F7F2EA"))
    d.rectangle(P(70, 460, 1130, 640), fill=shade(facade, 0.9))
    for x in range(70, 1130, 176):
        d.rectangle(P(x + 8, 470, x + 168, 630), outline=shade(facade, 0.75), width=3 * S)
        d.rectangle(P(x + 70, 490, x + 106, 498), fill=hexc("#F6C98E"))
    d.rectangle(P(820, 434, 980, 448), fill=hexc("#AEB9C6"))
    d.arc(P(880, 360, 940, 440), 180, 360, fill=hexc("#7C8A97"), width=6 * S)
    for x in (200, 300):
        d.ellipse(P(x, 434, x + 70, 452), fill=hexc("#2A2522"))
    # kettle, bowl, plant
    d.rounded_rectangle(P(420, 380, 490, 432), radius=16 * S, fill=hexc(accent))
    d.chord(P(560, 400, 660, 450), 0, 180, fill=hexc("#FFFFFF")); d.ellipse(P(580, 385, 610, 410), fill=hexc("#E2A23B")); d.ellipse(P(605, 388, 635, 413), fill=hexc("#C8654A"))
    plant(d, 1060, 432, "#3E6E5F")
    finish(img, path)

for code, spec in APTS.items():
    os.makedirs(os.path.join(OUT, code), exist_ok=True)
    exterior(code, spec, os.path.join(OUT, code, "main.jpg"))
    living(code, spec, os.path.join(OUT, code, "living.jpg"))
    bedroom(code, spec, os.path.join(OUT, code, "bedroom.jpg"))
    kitchen(code, spec, os.path.join(OUT, code, "kitchen.jpg"))
print("ok")
