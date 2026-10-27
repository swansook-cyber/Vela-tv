# Vela TV Launcher

Android TV launcher project for a TCL Android TV 11 (ARMv7), planned as a derivative of [CleanYourTV](https://github.com/Gathin23/cleanyourtv).

## Project safeguards
- Keep the original TCL launcher and system packages untouched during development.
- Do not disable system packages, modify the TV, or deploy automatically.
- Verify Android 11 / armeabi-v7a compatibility and D-pad/OK/Back before installing.
- Confirm a tested restore path before changing the default HOME handler.
- No advertisements or sponsored recommendations in the custom Home screen; no in-app ad blocking.

## Upstream attribution
CleanYourTV copyright (c) 2026 Gathin; upstream is MIT licensed. Preserve upstream copyright, license notices, and the separately licensed font notice when importing code.

## V3 development state (2026-10-27)
- Development branch: `develop/vela-tv-launcher`; `main` is not merged.
- The pinned upstream `launcher/` source at `0c0f69c3f1a50582bedc5569d4536367611ae9ff` is vendored here for deterministic review and testing. `LICENSE` and `launcher/FONT-LICENSE-OFL.txt` are preserved. Do not update upstream as part of stabilization.
- Application ID is fixed at `net.velalab.tvlauncher`, minSdk is 26, version name is `3.0.0`, and CI version code is `30000 + run_number`.
- V3 retains App Library, Home selection, hold-OK tile menu, order persistence, four dock slots, labels, and reduced motion support. Zero rows below the dock now places the next row beyond the viewport; banner artwork is fit without cropping.
- The launcher has no Internet permission or advertising dependency. Its Home does not contain sponsored rows. This does not remove ads inside other applications.
- Approved original logo, 512px icon, and 320x180 TV banner are committed under `assets/` with fixed SHA-256 values. The branding script uses these images without substituting artwork and produces legacy density icons plus an adaptive icon. The manifest points to the Vela assets, and CI compares embedded APK pixels with the approved images.
- CI requires the existing VelaLab keystore through `VELA_RELEASE_*` secrets and checks the certificate against `.github/vela-release-cert.sha256`. There is no debug signing fallback for release builds.
- Physical TCL device specifications, Home layout, focus, Input handling, and in-place upgrade remain unverified until an authorized device and a previous signed Vela TV release are available.

## Optional Vela TV Optimizer
`tools/vela_tv_optimizer.py` reads a supplied package-state JSON snapshot and produces a review report. It never connects to a TV or changes package state. Protected components are never proposed for disabling; any possible promotion or telemetry component requires explicit evidence and manual dependency review. A future change mode would require an approved exact package list and tested recovery path.

## Safe TCL acceptance and rollback
1. Keep the original TCL HOME app enabled; install and open the test APK from the TV's Apps list.
2. Check Vela TV Launcher name/banner, installed-app names/icons, App Library checkmarks, add/remove and reopen.
3. Check TCL remote D-pad, OK, long-press OK, Back, Settings, Inputs, power-cycle and return to Home.
4. Before approving Default HOME selection, identify the stock launcher package and confirm it can still be opened.
5. If a problem occurs before switching HOME, close or uninstall this test app from Android Settings > Apps.
6. If HOME was switched after separate approval, return to Android Settings > Apps > Default apps > Home app and select the stock launcher. Do not disable/delete system packages or grant privileged permissions during QA.
7. The launcher hides sponsored content only by replacing its own Home UI; it does not block ads within other apps or alter DNS/system services.
