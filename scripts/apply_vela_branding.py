#!/usr/bin/env python3
"""Install only the three approved Vela TV artwork files into Android resources."""
from hashlib import sha256
from pathlib import Path
import argparse
import shutil
import xml.etree.ElementTree as ET

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'assets'
RES = ROOT / 'launcher/app/src/main/res'
MANIFEST = ROOT / 'launcher/app/src/main/AndroidManifest.xml'
parser = argparse.ArgumentParser()
parser.add_argument('--verify-only', action='store_true')
args = parser.parse_args()

def install_image(path, expected):
    if args.verify_only:
        if not path.is_file():
            raise SystemExit(f'Missing approved Vela resource: {path}')
        with Image.open(path) as existing:
            actual = existing.convert(expected.mode)
        if actual.size != expected.size or actual.tobytes() != expected.tobytes():
            raise SystemExit(f'Vela resource pixels differ from approved artwork: {path}')
    else:
        path.parent.mkdir(parents=True, exist_ok=True)
        expected.save(path)

def install_text(path, expected):
    if args.verify_only:
        if not path.is_file() or path.read_text(encoding='utf-8') != expected:
            raise SystemExit(f'Vela resource text differs from approved artwork: {path}')
    else:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(expected, encoding='utf-8')
EXPECTED = {
    'vela-tv-original.png': '02f03b32d0053239a29ecf3e7ddb2f444c4cda1d3ecf054d6e0f3bb007e614d9',
    'vela-tv-icon-512.png': '3ebc266e21b5d8288811813f4ede655ed28ea45323d68ea1a7b6c9f81bd3c724',
    'vela-tv-banner-320x180.png': 'c7e77ea9aaf67bff2bbf105449697d720fe2270e194dde59ef89bda4cc500d50',
}
if (ASSETS / 'vela-tv-original.sha256').read_text(encoding='ascii').strip() != EXPECTED['vela-tv-original.png']:
    raise SystemExit('Approved original logo fingerprint file changed')
approved_lines = {name: digest for digest, name in (
    line.split() for line in (ASSETS / 'vela-tv-approved.sha256').read_text(encoding='ascii').splitlines()
)}
if approved_lines != EXPECTED:
    raise SystemExit('Approved Vela artwork manifest changed')
for name, digest in EXPECTED.items():
    source = ASSETS / name
    if not source.is_file() or sha256(source.read_bytes()).hexdigest() != digest:
        raise SystemExit(f'Approved Vela artwork missing or changed: {name}')

icon = Image.open(ASSETS / 'vela-tv-icon-512.png').convert('RGB')
banner = Image.open(ASSETS / 'vela-tv-banner-320x180.png').convert('RGB')
if icon.size != (512, 512) or banner.size != (320, 180):
    raise SystemExit('Approved icon or TV banner dimensions changed')
for density, px in {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144, 'xxxhdpi': 192}.items():
    path = RES / f'mipmap-{density}/vela_tv_icon.png'
    install_image(path, icon.resize((px, px), Image.Resampling.LANCZOS))

# Exact approved banner bytes are embedded, without recoloring or crop.
target = RES / 'drawable-nodpi/vela_tv_banner.png'
if args.verify_only:
    if not target.is_file() or target.read_bytes() != (ASSETS / 'vela-tv-banner-320x180.png').read_bytes():
        raise SystemExit('Embedded TV banner differs from approved original bytes')
else:
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(ASSETS / 'vela-tv-banner-320x180.png', target)

# Android's adaptive mask keeps the entire approved logo in its 72/108 safe zone.
foreground = Image.new('RGBA', (648, 648), (0, 0, 0, 0))
foreground.alpha_composite(icon.resize((432, 432), Image.Resampling.LANCZOS).convert('RGBA'), (108, 108))
foreground_path = RES / 'drawable-nodpi/vela_tv_adaptive_foreground.png'
install_image(foreground_path, foreground)
xml = RES / 'mipmap-anydpi-v26/vela_tv_icon.xml'
install_text(xml, '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/vela_tv_icon_background" />
    <foreground android:drawable="@drawable/vela_tv_adaptive_foreground" />
</adaptive-icon>
''')
colors = RES / 'values/vela_brand_colors.xml'
install_text(colors, '''<?xml version="1.0" encoding="utf-8"?>
<resources><color name="vela_tv_icon_background">#05040B</color></resources>
''')

manifest = MANIFEST.read_text(encoding='utf-8')
for old, new in (
    ('android:icon="@mipmap/ic_launcher"', 'android:icon="@mipmap/vela_tv_icon"'),
    ('android:banner="@drawable/banner"', 'android:banner="@drawable/vela_tv_banner"'),
):
    if old in manifest:
        if args.verify_only:
            raise SystemExit(f'Manifest still uses old branding reference: {old}')
        if manifest.count(old) != 1:
            raise SystemExit(f'Unexpected duplicate manifest reference: {old}')
        manifest = manifest.replace(old, new)
    elif manifest.count(new) != 1:
        raise SystemExit(f'Manifest branding reference changed: {new}')
if not args.verify_only:
    MANIFEST.write_text(manifest, encoding='utf-8')
for path in (MANIFEST, xml, colors):
    ET.parse(path)
print('Approved Vela original, icon, TV banner and adaptive icon verified')
