#!/usr/bin/env python3
"""Build Vela resources only from the approved original artwork."""
from pathlib import Path
from hashlib import sha256
import xml.etree.ElementTree as ET

from PIL import Image

SOURCE = Path('assets/vela-tv-original.png')
APPROVED = Path('assets/vela-tv-original.sha256')
RES = Path('launcher/app/src/main/res')
MANIFEST = Path('launcher/app/src/main/AndroidManifest.xml')
if not SOURCE.is_file() or not APPROVED.is_file():
    raise SystemExit('Original approved Vela TV artwork and SHA-256 are required')
actual = sha256(SOURCE.read_bytes()).hexdigest()
if actual != APPROVED.read_text(encoding='utf-8').strip().lower():
    raise SystemExit('Original Vela TV artwork differs from approved SHA-256')
image = Image.open(SOURCE).convert('RGBA')
if image.width < 512 or image.height < 512:
    raise SystemExit('Original artwork must be at least 512 x 512')

# Fit without cropping. The source pixels retain their proportions on dark Vela canvas.
def fitted(size):
    canvas = Image.new('RGBA', size, (17, 24, 39, 255))
    art = image.copy()
    art.thumbnail(size, Image.Resampling.LANCZOS)
    canvas.alpha_composite(art, ((size[0] - art.width) // 2, (size[1] - art.height) // 2))
    return canvas.convert('RGB')

for density, px in {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144, 'xxxhdpi': 192}.items():
    target = RES / ('mipmap-' + density) / 'vela_tv_icon.png'
    target.parent.mkdir(parents=True, exist_ok=True)
    fitted((px, px)).save(target)
banner = RES / 'drawable-nodpi/vela_tv_banner.png'
banner.parent.mkdir(parents=True, exist_ok=True)
fitted((640, 360)).save(banner)
manifest = MANIFEST.read_text(encoding='utf-8')
manifest = manifest.replace('android:icon="@mipmap/ic_launcher"', 'android:icon="@mipmap/vela_tv_icon"')
manifest = manifest.replace('android:banner="@drawable/banner"', 'android:banner="@drawable/vela_tv_banner"')
if manifest.count('android:icon="@mipmap/vela_tv_icon"') != 1 or manifest.count('android:banner="@drawable/vela_tv_banner"') != 1:
    raise SystemExit('Manifest branding references changed; inspect before building')
MANIFEST.write_text(manifest, encoding='utf-8')
ET.parse(MANIFEST)
print('Approved Vela artwork verified and rendered: ' + actual)
