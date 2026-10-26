#!/usr/bin/env python3
"""Non-destructive Vela TV branding overlay for pinned CleanYourTV checkout."""
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path("upstream/launcher/app/src/main")
strings = root / "res/values/strings.xml"
manifest = root / "AndroidManifest.xml"
styles = root / "res/values/styles.xml"
for file in (strings, manifest, styles):
    if not file.is_file():
        raise SystemExit(f"Missing upstream file: {file}")

source = strings.read_text(encoding="utf-8")
old = '<string name="app_name">Home</string>'
if source.count(old) != 1:
    raise SystemExit("Upstream app_name changed: inspect before patching")
source = source.replace(old, '<string name="app_name">Vela TV Launcher</string>')
source = source.replace("CleanYourTV", "Vela TV Launcher")
strings.write_text(source, encoding="utf-8")

# Retain upstream HOME, LEANBACK and rollback behavior.
# Only substitute app icon; original banner is retained until TV artwork QA.
vector = root / "res/drawable/vela_tv_icon.xml"
vector.write_text("""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#111827" android:pathData="M0,0h108v108h-108z"/>
    <path android:fillColor="#F3F4F6" android:pathData="M19,29h70v50h-70z"/>
    <path android:fillColor="#111827" android:pathData="M24,34h60v40h-60z"/>
    <path android:fillColor="#EC4899" android:pathData="M30,42l24,26 24,-26 -9,0 -15,17 -15,-17z"/>
</vector>
""", encoding="utf-8")
m = manifest.read_text(encoding="utf-8")
old_icon = 'android:icon="@mipmap/ic_launcher"'
if m.count(old_icon) != 1:
    raise SystemExit("Upstream manifest icon changed")
manifest.write_text(m.replace(old_icon, 'android:icon="@drawable/vela_tv_icon"'), encoding="utf-8")

# Thai Android TV locale overrides the default app name unless explicitly patched.
thai = root / "res/values-th/strings.xml"
thai_text = thai.read_text(encoding="utf-8")
thai_name = '<string name="app_name">หน้าแรก</string>'
if thai_text.count(thai_name) != 1:
    raise SystemExit("Thai launcher label changed upstream; inspect before patching")
thai.write_text(thai_text.replace(thai_name, '<string name="app_name">Vela TV Launcher</string>'), encoding="utf-8")

# V2: balanced dock spacing and one visible app row under the dock.
# Preserve all focus handling, D-pad behavior and app launch logic upstream.
config = root / "res/values/config.xml"
cfg = config.read_text(encoding="utf-8")
for before, after in (
    ('<bool name="dock_even_padding">false</bool>', '<bool name="dock_even_padding">true</bool>'),
    ('<integer name="rows_below_dock">0</integer>', '<integer name="rows_below_dock">1</integer>'),
    ('<bool name="app_library">false</bool>', '<bool name="app_library">true</bool>'),
):
    if cfg.count(before) != 1:
        raise SystemExit(f"Upstream layout setting changed: {before}")
    cfg = cfg.replace(before, after)
config.write_text(cfg, encoding="utf-8")

# Ensure resources and manifest remain well-formed XML.
for file in (strings, thai, manifest, styles, vector, config):
    ET.parse(file)
print("Vela TV Launcher V2 branding and XML validated")
