# Vela TV Launcher implementation gates

## Baseline
- Upstream: https://github.com/Gathin23/cleanyourtv (pin an exact commit before import).
- Import upstream `launcher/` sources and build files without running any install/debloat script.
- Preserve upstream MIT LICENSE and Outfit OFL notice when font assets are imported.
- Device: TCL Android TV 11, MediaTek MT9221, ARMv7 (32-bit). Confirm actual device ABI with read-only probe.
- Upstream baseline: Java, minSdk 26, compileSdk 34, JDK 17; validate against imported files.

## Development gates
1. Record upstream commit SHA, license, dependency inventory, and build reproduction.
2. Build unchanged upstream launcher APK in GitHub Actions; upload artifact. Never execute installation scripts in CI.
3. Rebrand app name, icon, and unique applicationId `net.velalab.tvlauncher` without changing the upstream source namespace unnecessarily.
4. UI: Apple TV-inspired (not copied Apple assets), dark home, four pinned apps, full app grid, accessible D-pad focus, OK/Back and long-press; Thai language.
5. Home contains no ads, sponsored rows, or external recommendation feed. No in-app ad blocking, DNS modifications, or telemetry debloat.
6. Test cold boot, app launching, focus restoration, memory use, offline use, and stock launcher recovery on TCL Android 11.
7. Only after approval: investigate HOME picker; back up original HOME handler and installed package list. Do not disable the stock launcher without verified rollback and explicit approval.

## Non-destructive policy
No automatic deployment, no ADB package disabling, no TV settings writes, no merge to main, no production changes. Build APK must be tested before claiming readiness.

## Current verification
- The pinned launcher source is imported into this repository; unit tests and a local debug build pass.
- The original Vela artwork was supplied and its original, square icon, and TV banner SHA-256 values are pinned in `assets/`. Resource verification compares the embedded APK artwork against those assets.
- The release certificate baseline is `965f96aeca629a3ea78f439d4fd5fe3bbee53844ff8f445b7fc068b9d85f2dbe`, observed in existing VelaLab APKs. A Vela TV signed release has not yet been established.
- Physical TCL D-pad, long OK, Back, launch/return, performance, actual ABI, and stock HOME rollback still require device QA. No device or default HOME is changed by the build.
- Builds do not remove ads inside third-party apps, change DNS, disable any system package, or set Default HOME.
