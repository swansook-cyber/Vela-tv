# Vela TV Launcher V3 layout and release QA — 2026-10-27

## Layout finding
The previous zero-row dock layout subtracted `BOTTOM_ROOM_DP` (30dp) from the top padding even when `rows_below_dock=0`. That left the beginning of the next RecyclerView row inside the visible viewport. `HomeViewport.dockTopPadding` now reserves no below-dock room when zero rows are requested. The next row begins at or below the viewport edge and remains reachable with Down. App banners now use `fitCenter` rather than `centerCrop`.

## Automated evidence
- Local `:app:testDebugUnitTest :app:assembleDebug`: PASS.
- `HomeViewportTest`: 3 tests, 0 failures (480/720/846/1080px viewport scenarios and multiple densities).
- `TileFitTest`: 6 tests, 0 failures.
- `GridNavigationTest`: 6 tests, 0 failures.
- `DockSlotsTest`: 12 tests, 0 failures.
- `OrderTest`: 10 tests, 0 failures.
- `scripts/verify_apk_branding.py` on local debug APK: PASS. Embedded banner pixels match the approved 320x180 image; all five density icons match approved icon resampling; adaptive resources are present.
- GitHub Actions run #19, commit `9257e1ff7c64b699fca27e9a1ba6ea4d54514287`: validate PASS; release FAIL at missing signing secrets before build.

## Limits
The layout tests verify geometry and navigation logic, not rendered output on the physical TCL. Actual panel resolution, density, overscan, D-pad focus visibility, Up/Down behavior, Back, Settings, Inputs, and cold restart remain NOT TESTED on that device. No Default HOME or TV package state was changed.

## Release blocker
GitHub Actions run #19 reported `VELA_RELEASE_KEYSTORE_BASE64` missing. The job environment also showed `VELA_RELEASE_STORE_PASSWORD`, `VELA_RELEASE_KEY_PASSWORD`, and `VELA_RELEASE_KEY_ALIAS` unset. Configure these as GitHub Actions secrets using the established VelaLab signing identity; do not send their values in chat. The release job verifies the produced certificate against `.github/vela-release-cert.sha256` and will fail on a mismatch. No V3 signed APK or in-place upgrade result exists yet.
