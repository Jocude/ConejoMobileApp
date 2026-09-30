"""Genera todas las imágenes de Conejo Jorge (estilo plano, contorno oscuro) con pycairo + Pillow.

Uso: python3 generar_imagenes.py <carpeta res/> [carpeta para vistas previas en PNG]
Ejemplo, desde la raíz del repo: python3 Documentacion/arte/generar_imagenes.py ConejoJorge/app/src/main/res
"""
import math, os, io, sys
import cairo
from PIL import Image, ImageFont, ImageDraw, ImageFilter

RES = sys.argv[1]  # carpeta res/ del proyecto
FONT = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'fuentes', 'Fredoka.ttf')
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else None
if PREVIEW:
    os.makedirs(PREVIEW, exist_ok=True)

INK = '#3b2a3f'
FUR = '#f4f1f6'
PINK = '#f7a8c4'
NOSE = '#e56b92'
TAU = 2 * math.pi


def rgb(h, a=1.0):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) / 255 for i in (0, 2, 4)) + (a,)


def ellipse(c, x, y, rx, ry, rot=0.0):
    c.save(); c.translate(x, y); c.rotate(rot); c.scale(rx, ry)
    c.new_sub_path(); c.arc(0, 0, 1, 0, TAU); c.restore()


def paint(c, fill=None, line=None, width=0.0, alpha=1.0):
    if fill:
        c.set_source_rgba(*rgb(fill, alpha)); c.fill_preserve()
    if line:
        c.set_source_rgba(*rgb(line)); c.set_line_width(width); c.stroke_preserve()
    c.new_path()


def render(draw, base_w, base_h, out_w, out_h=None):
    """Dibuja en coordenadas base (base_w x base_h) y lo devuelve como imagen de Pillow a out_w de ancho."""
    out_h = out_h or round(out_w * base_h / base_w)
    s = cairo.ImageSurface(cairo.FORMAT_ARGB32, out_w, out_h)
    c = cairo.Context(s)
    c.scale(out_w / base_w, out_h / base_h)
    c.set_line_join(cairo.LINE_JOIN_ROUND); c.set_line_cap(cairo.LINE_CAP_ROUND)
    draw(c)
    buf = io.BytesIO(); s.write_to_png(buf); buf.seek(0)
    return Image.open(buf).convert('RGBA')


# ---------------- Personaje ----------------

def rabbit(c):
    """Conejo en un lienzo de 248x320 (proporción 62x80 del sprite)."""
    lw = 8
    for sx in (-1, 1):  # orejas
        ellipse(c, 124 + sx * 46, 70, 25, 62, sx * 0.16); paint(c, FUR, INK, lw)
        ellipse(c, 124 + sx * 46, 76, 12, 44, sx * 0.16); paint(c, PINK)
    ellipse(c, 124, 272, 84, 44); paint(c, FUR, INK, lw)          # cuerpo
    ellipse(c, 124, 280, 46, 30); paint(c, '#ffffff')             # barriga
    for sx in (-1, 1):                                             # patas
        ellipse(c, 124 + sx * 52, 302, 30, 13); paint(c, FUR, INK, lw)
    ellipse(c, 124, 168, 104, 84); paint(c, FUR, INK, lw)         # cabeza
    for sx in (-1, 1):                                             # mejillas
        ellipse(c, 124 + sx * 60, 196, 17, 12); paint(c, PINK, alpha=0.75)
    for sx in (-1, 1):                                             # ojos
        ellipse(c, 124 + sx * 36, 162, 14, 18); paint(c, INK)
        ellipse(c, 124 + sx * 36 + 5, 154, 5.5, 5.5); paint(c, '#ffffff')
    c.move_to(112, 186); c.line_to(136, 186); c.line_to(124, 198); c.close_path(); paint(c, NOSE)
    c.move_to(124, 198); c.line_to(124, 205)
    c.move_to(124, 205); c.curve_to(118, 214, 107, 212, 104, 204)
    c.move_to(124, 205); c.curve_to(130, 214, 141, 212, 144, 204)
    paint(c, line=INK, width=5)


# ---------------- Pincho ----------------

def spike(c, rot):
    """Estrella de púas metálica en un lienzo de 280x280. Radio del cuerpo ~0.30 del ancho."""
    c.translate(140, 140); c.rotate(rot)
    n = 8
    for i in range(n * 2):
        a = i * math.pi / n
        r = 132 if i % 2 == 0 else 86
        c.line_to(r * math.cos(a), r * math.sin(a))
    c.close_path(); paint(c, '#9aa6ba', INK, 8)
    for i in range(n):  # cara iluminada de cada púa
        a = i * TAU / n
        c.move_to(0, 0); c.line_to(126 * math.cos(a), 126 * math.sin(a))
        c.line_to(86 * math.cos(a + math.pi / n), 86 * math.sin(a + math.pi / n)); c.close_path()
        c.set_source_rgba(1, 1, 1, 0.3); c.fill()
    ellipse(c, 0, 0, 80, 80); paint(c, '#4a4e69', INK, 8)
    ellipse(c, 0, 0, 44, 44); paint(c, '#e63946', INK, 6)
    c.rotate(-rot)  # el brillo no gira: la luz viene siempre de arriba a la izquierda
    ellipse(c, -14, -14, 12, 12); c.set_source_rgba(1, 1, 1, 0.75); c.fill()
    ellipse(c, 0, 0, 72, 72); c.set_line_width(6); c.set_source_rgba(1, 1, 1, 0.12); c.stroke()


# ---------------- Explosión (polvo al tocar el suelo) ----------------

def puff_frame(c, t):
    """Nube de polvo en un lienzo de 320x288 (80x72). t = 0, 1, 2."""
    cx, cy = 160, 150
    spread = [30, 78, 104][t]
    size = [34, 30, 20][t]
    alpha = [1.0, 0.95, 0.7][t]
    if t == 0:  # destello central
        for i in range(8):
            a = i * TAU / 8 + 0.2
            c.move_to(cx + 20 * math.cos(a), cy + 20 * math.sin(a))
            c.line_to(cx + 70 * math.cos(a), cy + 70 * math.sin(a))
        paint(c, line='#ffd166', width=10)
    n = 7 if t else 5
    for i in range(n):
        a = i * TAU / n - math.pi / 2 + t * 0.3
        r = size * (1.0 if i % 2 else 0.8)
        ellipse(c, cx + spread * math.cos(a), cy + spread * math.sin(a) * 0.85, r, r)
        paint(c, '#fffaf0', '#c9bfb4', 6, alpha)
        ellipse(c, cx + spread * math.cos(a) - r * 0.3, cy + spread * math.sin(a) * 0.85 - r * 0.3, r * 0.35, r * 0.35)
        paint(c, '#ffffff', alpha=alpha)
    if t == 0:
        ellipse(c, cx, cy, 36, 36); paint(c, '#fffaf0', '#c9bfb4', 6)


# ---------------- Escenario ----------------

def ground(c):
    """Tramo de suelo de 480x100 (96x20) que se repite sin costuras: césped arriba y tierra abajo."""
    W, H = 480, 100
    c.rectangle(0, 0, W, H); c.set_source_rgba(*rgb('#a0673c')); c.fill()
    for (x, y, r) in [(40, 70, 9), (130, 84, 6), (210, 64, 7), (300, 80, 10), (380, 66, 6), (450, 88, 7)]:
        ellipse(c, x, y, r * 1.4, r); c.set_source_rgba(*rgb('#8a5530')); c.fill()
    c.rectangle(0, 0, W, 34); c.set_source_rgba(*rgb('#6ab04c')); c.fill()
    # borde ondulado del césped (periodo que divide el ancho: sin costuras al repetir)
    c.move_to(0, 30)
    for i in range(0, W + 1, 4):
        c.line_to(i, 34 + 7 * math.sin(i / W * TAU * 6))
    c.line_to(W, 0); c.line_to(0, 0); c.close_path(); c.set_source_rgba(*rgb('#6ab04c')); c.fill()
    c.rectangle(0, 0, W, 10); c.set_source_rgba(*rgb('#8fd16a')); c.fill()
    for i in range(12):  # briznas
        x = 20 + i * 40
        c.move_to(x, 12); c.line_to(x + 5, 3); c.line_to(x + 10, 12)
        c.set_source_rgba(*rgb('#8fd16a')); c.set_line_width(4); c.stroke()


def background(c, W, H):
    g = cairo.LinearGradient(0, 0, 0, H)
    g.add_color_stop_rgb(0, *rgb('#6fb7e0')[:3]); g.add_color_stop_rgb(0.7, *rgb('#d4efff')[:3])
    c.set_source(g); c.paint()
    # sol
    for r, a in [(190, 0.15), (150, 0.25)]:
        ellipse(c, W * 0.8, H * 0.13, r, r); c.set_source_rgba(*rgb('#fff3b0', a)); c.fill()
    ellipse(c, W * 0.8, H * 0.13, 110, 110); c.set_source_rgba(*rgb('#ffe066')); c.fill()
    # nubes
    for (x, y, s) in [(0.22, 0.12, 1.0), (0.62, 0.27, 0.8), (0.18, 0.42, 0.65), (0.85, 0.5, 0.55)]:
        for dx, dy, r in [(-100, 10, 80), (0, -35, 105), (105, 10, 75), (0, 30, 85)]:
            ellipse(c, W * x + dx * s, H * y + dy * s, r * s, r * s)
            c.set_source_rgba(1, 1, 1, 0.92); c.fill()
    # colinas en tres planos
    for col, base, amp, f, ph in [('#b7e4c7', 0.74, 0.05, 1.4, 0.3), ('#95d5b2', 0.81, 0.04, 2.2, 1.7),
                                   ('#74c69d', 0.88, 0.03, 3.1, 0.9)]:
        c.move_to(0, H)
        for i in range(0, W + 1, 6):
            c.line_to(i, H * base + math.sin(i / W * math.pi * f + ph) * H * amp)
        c.line_to(W, H); c.close_path(); c.set_source_rgba(*rgb(col)); c.fill()
    # arbolitos en la colina del medio
    for x in (0.12, 0.3, 0.7, 0.9):
        px = W * x
        py = H * 0.81 + math.sin(px / W * math.pi * 2.2 + 1.7) * H * 0.04
        c.rectangle(px - 8, py - 30, 16, 40); c.set_source_rgba(*rgb('#8a5a3c')); c.fill()
        ellipse(c, px, py - 60, 42, 50); c.set_source_rgba(*rgb('#52b788')); c.fill()


# ---------------- Interfaz ----------------

def round_button(c, color, icon):
    """Botón redondo de 320x320."""
    ellipse(c, 160, 168, 146, 146); c.set_source_rgba(*rgb(INK, 0.25)); c.fill()  # sombra
    ellipse(c, 160, 158, 146, 146); paint(c, color, INK, 10)
    ellipse(c, 160, 128, 112, 90); c.set_source_rgba(1, 1, 1, 0.18); c.fill()     # brillo
    c.set_source_rgba(1, 1, 1, 1); c.set_line_width(30)
    icon(c)


def icon_restart(c):
    """Flecha circular en el sentido de las agujas del reloj, con la punta arriba a la derecha."""
    cx, cy, r = 160, 158, 70
    a0, a1 = math.radians(10), math.radians(285)
    c.arc(cx, cy, r, a0, a1); c.stroke()
    px, py = cx + r * math.cos(a1), cy + r * math.sin(a1)
    tx, ty = -math.sin(a1), math.cos(a1)   # tangente (sentido de avance)
    nx, ny = math.cos(a1), math.sin(a1)    # normal hacia fuera
    c.move_to(px + tx * 44, py + ty * 44)
    c.line_to(px + nx * 38, py + ny * 38); c.line_to(px - nx * 38, py - ny * 38); c.close_path(); c.fill()


def icon_power(c):
    c.arc(160, 166, 70, math.radians(-50), math.radians(230)); c.stroke()
    c.move_to(160, 72); c.line_to(160, 160); c.stroke()


def star_path(c, cx, cy, ro, ri, n=5, rot=-math.pi / 2):
    for i in range(n * 2):
        r = ro if i % 2 == 0 else ri
        a = rot + i * math.pi / n
        c.line_to(cx + r * math.cos(a), cy + r * math.sin(a))
    c.close_path()


def trophy(c):
    """Copa de récord en 400x440."""
    for i in range(12):  # rayos
        a = i * TAU / 12
        c.move_to(200, 190)
        c.line_to(200 + 200 * math.cos(a - 0.13), 190 + 200 * math.sin(a - 0.13))
        c.line_to(200 + 200 * math.cos(a + 0.13), 190 + 200 * math.sin(a + 0.13)); c.close_path()
    c.set_source_rgba(*rgb('#ffe066', 0.45)); c.fill()
    lw = 10
    for sx in (-1, 1):  # asas
        ellipse(c, 200 + sx * 118, 150, 52, 58); paint(c, line=INK, width=lw + 20)
        ellipse(c, 200 + sx * 118, 150, 52, 58); paint(c, line='#f4b400', width=20)
    c.move_to(80, 70); c.line_to(320, 70); c.curve_to(320, 230, 270, 280, 200, 280)
    c.curve_to(130, 280, 80, 230, 80, 70); c.close_path(); paint(c, '#ffc93c', INK, lw)
    c.move_to(110, 90); c.curve_to(108, 180, 130, 230, 165, 256); c.line_to(150, 256)
    c.curve_to(112, 226, 96, 170, 96, 90); c.close_path(); c.set_source_rgba(1, 1, 1, 0.4); c.fill()
    c.rectangle(176, 276, 48, 60); paint(c, '#f4b400', INK, lw)
    c.new_sub_path(); rrect(c, 120, 330, 160, 70, 14); paint(c, '#a0673c', INK, lw)
    c.new_sub_path(); rrect(c, 150, 350, 100, 28, 8); paint(c, '#ffc93c')
    star_path(c, 200, 165, 64, 28); paint(c, '#ffffff', INK, 7)


def rrect(c, x, y, w, h, r):
    c.arc(x + w - r, y + r, r, -math.pi / 2, 0); c.arc(x + w - r, y + h - r, r, 0, math.pi / 2)
    c.arc(x + r, y + h - r, r, math.pi / 2, math.pi); c.arc(x + r, y + r, r, math.pi, 1.5 * math.pi)
    c.close_path()


def title(width):
    """Título «Save the Bunny» con Fredoka Bold, contorno y sombra."""
    def font(size):
        f = ImageFont.truetype(FONT, size)
        try:
            f.set_variation_by_name('Bold')
        except Exception:
            pass
        return f
    lines = [('SAVE THE', font(150), '#ffd166'), ('BUNNY', font(250), '#ff8fab')]
    stroke = 22
    pad = 40
    heights = []
    widths = []
    for text, f, _ in lines:
        l, t, r, b = f.getbbox(text, stroke_width=stroke)
        widths.append(r - l); heights.append((t, b))
    W = max(widths) + pad * 2
    H = sum(b - t for t, b in heights) + pad * 2 - 20
    img = Image.new('RGBA', (W, H + 20), (0, 0, 0, 0))
    shadow = Image.new('RGBA', img.size, (0, 0, 0, 0))
    d, ds = ImageDraw.Draw(img), ImageDraw.Draw(shadow)
    y = pad
    for (text, f, color), w, (t, b) in zip(lines, widths, heights):
        x = (W - w) // 2
        ds.text((x, y - t + 16), text, font=f, fill=(59, 42, 63, 110), stroke_width=stroke, stroke_fill=(59, 42, 63, 110))
        d.text((x, y - t), text, font=f, fill=color, stroke_width=stroke, stroke_fill=INK)
        # brillo: la mitad superior de cada letra algo más clara
        y += (b - t) - 20
    out = Image.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(4)), img)
    out = out.crop(out.getbbox())
    return out.resize((width, round(out.height * width / out.width)), Image.LANCZOS)


# ---------------- Guardado ----------------

def save_webp(img, path, lossless=True):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    if lossless:
        img.save(path, 'WEBP', lossless=True, quality=100, method=6)
    else:
        img.save(path, 'WEBP', quality=90, method=6)


NODPI = os.path.join(RES, 'drawable-nodpi')
S = 5  # píxeles por unidad de diseño de los sprites

sprites = {
    'rabbit': render(rabbit, 248, 320, 62 * S),
    'spike0': render(lambda c: spike(c, 0), 280, 280, 68 * S),
    'spike1': render(lambda c: spike(c, math.radians(15)), 280, 280, 68 * S),
    'spike2': render(lambda c: spike(c, math.radians(30)), 280, 280, 68 * S),
    'explode0': render(lambda c: puff_frame(c, 0), 320, 288, 80 * S),
    'explode1': render(lambda c: puff_frame(c, 1), 320, 288, 80 * S),
    'explode2': render(lambda c: puff_frame(c, 2), 320, 288, 80 * S),
    'ground': render(ground, 480, 100, 96 * S),
    'restart': render(lambda c: round_button(c, '#52b788', icon_restart), 320, 320, 320),
    'exit': render(lambda c: round_button(c, '#e63946', icon_power), 320, 320, 320),
    'new_highest': render(trophy, 400, 440, 400),
    'game_title': title(1000),
}
for name, img in sprites.items():
    save_webp(img, os.path.join(NODPI, name + '.webp'))
    if PREVIEW:
        img.save(os.path.join(PREVIEW, name + '.png'))
bg = render(lambda c: background(c, 1080, 2340), 1080, 2340, 1080)
save_webp(bg.convert('RGB'), os.path.join(NODPI, 'background.webp'), lossless=False)
if PREVIEW:
    bg.save(os.path.join(PREVIEW, 'background.png'))

# ---------------- Icono ----------------

def icon_foreground(size):
    """Capa frontal del icono adaptativo (108 dp): el conejo dentro de la zona segura de 66 dp."""
    h = size * 0.64
    r = render(rabbit, 248, 320, round(h * 248 / 320))
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    img.alpha_composite(r, ((size - r.width) // 2, round(size * 0.53 - r.height / 2)))
    return img


def icon_monochrome(fg):
    """Silueta para los iconos temáticos de Android 13+: solo las zonas claras, sin contornos ni ojos."""
    px = fg.load()
    out = Image.new('RGBA', fg.size, (0, 0, 0, 0))
    po = out.load()
    for y in range(fg.height):
        for x in range(fg.width):
            r, g, b, a = px[x, y]
            lum = (0.3 * r + 0.59 * g + 0.11 * b) / 255
            if a and lum > 0.55:
                po[x, y] = (255, 255, 255, a)
    return out


ICON_BG = (111, 183, 224, 255)  # #6FB7E0, el azul del cielo
for dens, fg_size, legacy in [('mdpi', 108, 48), ('hdpi', 162, 72), ('xhdpi', 216, 96), ('xxhdpi', 324, 144), ('xxxhdpi', 432, 192)]:
    folder = os.path.join(RES, 'mipmap-' + dens)
    fg = icon_foreground(fg_size)
    save_webp(fg, os.path.join(folder, 'ic_launcher_foreground.webp'))
    save_webp(icon_monochrome(fg), os.path.join(folder, 'ic_launcher_monochrome.webp'))
    # Iconos antiguos (sin capas): se recortan 18 dp por lado de la versión adaptativa
    full = Image.new('RGBA', fg.size, ICON_BG); full.alpha_composite(fg)
    m = round(fg_size * 18 / 108)
    full = full.crop((m, m, fg_size - m, fg_size - m)).resize((legacy, legacy), Image.LANCZOS)
    for name, shape in [('ic_launcher', 'square'), ('ic_launcher_round', 'circle')]:
        big = Image.new('L', (legacy * 4, legacy * 4), 0)
        dm = ImageDraw.Draw(big)
        if shape == 'circle':
            dm.ellipse((0, 0, legacy * 4 - 1, legacy * 4 - 1), fill=255)
        else:
            dm.rounded_rectangle((0, 0, legacy * 4 - 1, legacy * 4 - 1), radius=legacy * 4 // 5, fill=255)
        mask = big.resize((legacy, legacy), Image.LANCZOS)
        out = Image.new('RGBA', (legacy, legacy), (0, 0, 0, 0)); out.paste(full, (0, 0), mask)
        save_webp(out, os.path.join(folder, name + '.webp'))
    if PREVIEW and dens == 'xxxhdpi':
        prev = Image.new('RGBA', fg.size, ICON_BG); prev.alpha_composite(fg); prev.save(os.path.join(PREVIEW, 'icono.png'))
        icon_monochrome(fg).save(os.path.join(PREVIEW, 'icono_mono.png'))
print('ok')
