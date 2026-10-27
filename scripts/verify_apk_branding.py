#!/usr/bin/env python3
"""Verify Android package artwork against the approved Vela source pixels."""
from pathlib import Path
from zipfile import ZipFile
import argparse
import re
import subprocess
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('apk', type=Path)
parser.add_argument('--aapt', type=Path, required=True)
args = parser.parse_args()
source_icon = Image.open(ROOT / 'assets/vela-tv-icon-512.png').convert('RGB')
source_banner = Image.open(ROOT / 'assets/vela-tv-banner-320x180.png').convert('RGB')
def aapt(*arguments):
    return subprocess.check_output([str(args.aapt), 'dump', *arguments, str(args.apk)],
                                   encoding='utf-8', errors='replace')

badging = aapt('badging')
resources = aapt('resources')
def resource_id(name):
    match = re.search(r'spec resource (0x[0-9a-f]+) net\.velalab\.tvlauncher:' +
                      re.escape(name) + r':', resources)
    if not match:
        raise SystemExit(f'Missing named approved artwork resource: {name}')
    return match.group(1)

icon_id = resource_id('mipmap/vela_tv_icon')
banner_id = resource_id('drawable/vela_tv_banner')
foreground_id = resource_id('drawable/vela_tv_adaptive_foreground')
background_id = resource_id('color/vela_tv_icon_background')
manifest_tree = subprocess.check_output(
    [str(args.aapt), 'dump', 'xmltree', str(args.apk), 'AndroidManifest.xml'],
    encoding='utf-8', errors='replace')
if f'android:icon(0x01010002)=@{icon_id}' not in manifest_tree or \
        f'android:banner(0x010103f2)=@{banner_id}' not in manifest_tree:
    raise SystemExit('Application manifest does not reference approved branding resources')
icon_match = re.search(r"application-icon-160:'([^']+\.xml)'", badging)
banner_match = re.search(r"application: .* banner='([^']+\.png)'", badging)
if not icon_match or not banner_match:
    raise SystemExit('Vela adaptive icon or TV banner missing from application manifest')
icon_path, banner_path = icon_match.group(1), banner_match.group(1)
icon_tree = subprocess.check_output(
    [str(args.aapt), 'dump', 'xmltree', str(args.apk), icon_path],
    encoding='utf-8', errors='replace')
if not all(marker in icon_tree for marker in ('E: adaptive-icon', 'E: foreground', 'E: background',
                                            f'=@{foreground_id}', f'=@{background_id}')):
    raise SystemExit('Application icon is not an adaptive icon')
with ZipFile(args.apk) as apk:
    names = set(apk.namelist())
    def same_pixels(entry, expected, mode='RGB'):
        with apk.open(entry) as stream:
            actual = Image.open(stream).convert(mode)
            actual.load()
        if actual.size != expected.size or actual.tobytes() != expected.tobytes():
            raise SystemExit(f'Embedded artwork differs from approved image: {entry}')
    if icon_path not in names or banner_path not in names:
        raise SystemExit('Application branding paths are missing from APK')
    same_pixels(banner_path, source_banner)
    def find_pixels(expected, mode='RGB'):
        matches = []
        for entry in sorted(n for n in names if n.endswith('.png')):
            with apk.open(entry) as stream:
                image = Image.open(stream)
                if image.size == expected.size and image.convert(mode).tobytes() == expected.tobytes():
                    matches.append(entry)
        if len(matches) != 1:
            raise SystemExit(f'Expected one approved {expected.size} image in APK, found {len(matches)}')
        return matches[0]
    for density, size in {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96,
                          'xxhdpi': 144, 'xxxhdpi': 192}.items():
        find_pixels(source_icon.resize((size, size), Image.Resampling.LANCZOS))
    foreground = Image.new('RGBA', (648, 648), (0, 0, 0, 0))
    foreground.alpha_composite(source_icon.resize((432, 432), Image.Resampling.LANCZOS).convert('RGBA'), (108, 108))
    find_pixels(foreground, 'RGBA')
print('PASS: approved Vela TV icon densities, adaptive icon and full 16:9 banner are embedded')
