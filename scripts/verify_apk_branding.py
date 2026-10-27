#!/usr/bin/env python3
"""Verify Android package artwork against the approved Vela source pixels."""
from pathlib import Path
from zipfile import ZipFile
import argparse
from PIL import Image, ImageChops

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('apk', type=Path)
args = parser.parse_args()
source_icon = Image.open(ROOT / 'assets/vela-tv-icon-512.png').convert('RGB')
source_banner = Image.open(ROOT / 'assets/vela-tv-banner-320x180.png').convert('RGB')
with ZipFile(args.apk) as apk:
    names = apk.namelist()
    def resource(suffix):
        matches = [n for n in names if n.endswith(suffix)]
        if len(matches) != 1:
            raise SystemExit(f'Expected one embedded {suffix}, found {matches}')
        return matches[0]
    def same_pixels(entry, expected):
        with apk.open(entry) as stream:
            actual = Image.open(stream).convert('RGB')
            actual.load()
        if actual.size != expected.size or ImageChops.difference(actual, expected).getbbox():
            raise SystemExit(f'Embedded artwork differs from approved image: {entry}')
    same_pixels(resource('/vela_tv_banner.png'), source_banner)
    for density, size in {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96,
                          'xxhdpi': 144, 'xxxhdpi': 192}.items():
        matches = [n for n in names if n.startswith(f'res/mipmap-{density}-v4/') and n.endswith('/vela_tv_icon.png')]
        if len(matches) != 1:
            raise SystemExit(f'Missing embedded {density} Vela launcher icon')
        same_pixels(matches[0], source_icon.resize((size, size), Image.Resampling.LANCZOS))
    resource('/vela_tv_adaptive_foreground.png')
    resource('/vela_tv_icon.xml')
print('PASS: approved Vela TV icon densities, adaptive icon and full 16:9 banner are embedded')
