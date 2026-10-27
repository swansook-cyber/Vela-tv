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

## Current state (2026-10-27)
- Development branch: `develop/vela-tv-launcher`; `main` is not merged.
- V2 Build #12 passed on 2026-10-26, using pinned upstream revision `0c0f69c3f1a50582bedc5569d4536367611ae9ff`.
- CI downloads upstream `launcher/` into a temporary checkout; it is intentionally not committed to this repository.
- Debug package: `net.velalab.tvlauncher`, Android minSdk 26 (TCL Android TV 11 / 32-bit target).
- V2 enables App Library, Home selection, hold-OK tile actions, and one app row below the dock.
- This branch subsequently adds Vela TV banner, uniform installed-app icons, visible labels, and APK identity checks.
- No claim of release readiness until the updated APK is built and exercised on the real TCL TV.

## Safe TCL acceptance and rollback
1. Keep the original TCL HOME app enabled; install and open the test APK from the TV's Apps list.
2. Check Vela TV Launcher name/banner, installed-app names/icons, App Library checkmarks, add/remove and reopen.
3. Check TCL remote D-pad, OK, long-press OK, Back, Settings, Inputs, power-cycle and return to Home.
4. Before approving Default HOME selection, identify the stock launcher package and confirm it can still be opened.
5. If a problem occurs before switching HOME, close or uninstall this test app from Android Settings > Apps.
6. If HOME was switched after separate approval, return to Android Settings > Apps > Default apps > Home app and select the stock launcher. Do not disable/delete system packages or grant privileged permissions during QA.
7. The launcher hides sponsored content only by replacing its own Home UI; it does not block ads within other apps or alter DNS/system services.
