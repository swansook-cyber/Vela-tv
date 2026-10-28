# Vela TV Launcher V4 — Apple-style Home & Themes

Status: implementation planned; no V4 APK or physical QA yet.
Base: V3.1 commit f48fde2bb4fd72116efd8ddc44e4a55659c87c0d
Target: TCL Android TV 11, 1080p; D-pad/OK/Back first.

## Goals
1. Refine home into spacious Apple TV-inspired layout with rounded app cards, readable typography, clear focus and smooth transitions; original design, no copied assets.
2. Add built-in static wallpaper choices and user-selected JPG/PNG background using Android's document picker; persist URI permission where supported.
3. Add wallpaper preview/apply/reset and light/dark overlay for readability.
4. Keep app visibility, ordering, launching, settings, and existing launcher HOME behavior intact.
5. Performance: no network dependency; cache decoded/scaled bitmaps; avoid decoding on UI thread, use bounded memory; respect reduced motion setting.
6. No video wallpaper in initial V4 scope; reconsider only after physical TCL performance QA.

## Delivery gates
- Inspect existing Android UI, resources, manifest, Gradle, and test setup before coding.
- Unit/build checks and GitHub Actions signed artifact; keep release signing certificate and applicationId unchanged.
- Upgrade V3.1 to V4 without losing saved settings; rollback by reinstalling approved V3.1 if Android version code permits, otherwise plan explicit safe restore.
- TCL physical QA: startup/Home, D-pad/OK/Back, all app tiles, show/hide persistence, image import/preview/reset, cold boot, 30-minute navigation/performance.
- No main merge, App Store publication, system launcher disable, or production changes without explicit approval.

## Design notes
- Use CleanYourTV as functional reference only; do not copy proprietary images, artwork, source code, or brand assets.
- Default to static wallpaper and subtle 150–220 ms focus animation, with an off/reduced-motion option.
- Preserve existing accepted V3/V3.1 branding and icon unless separately approved.
