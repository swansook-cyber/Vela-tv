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

## Current blocker
Upstream source archive is not yet imported; repository access in the current execution environment does not permit downloading the upstream Git tree. Do not claim a working APK until the source is actually imported and CI passes.
