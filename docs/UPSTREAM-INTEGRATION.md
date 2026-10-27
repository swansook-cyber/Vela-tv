# Vela TV Launcher — upstream integration

Upstream: https://github.com/Gathin23/cleanyourtv (MIT; retain LICENSE and launcher/FONT-LICENSE-OFL.txt when copying assets).

## Confirmed structure
The native Android app is under `launcher/`, NOT the repository root.
- `launcher/build.gradle`: Android Gradle Plugin 8.5.2.
- `launcher/app/build.gradle`: Java 17, compileSdk 34, minSdk 26, targetSdk 34; AndroidX RecyclerView and DynamicAnimation.
- `launcher/app/src/main/AndroidManifest.xml`: Android TV launcher manifest.
- Upstream default package: `com.gathin.tvhome`; Vela build must use a distinct applicationId.

## Safe import procedure (development branch only)
1. Fetch upstream source at a pinned commit and record SHA, including launcher Gradle wrapper, sources, resources, licenses, and tests. Do not copy scripts that disable TCL system packages.
2. Copy upstream `launcher/` into this repository, retaining attribution. Prefer distinct Vela applicationId `net.velalab.tvlauncher` via `-PlauncherApplicationId=net.velalab.tvlauncher` when building.
3. Run `./gradlew :app:assembleDebug :app:testDebugUnitTest` from launcher directory using Java 17. Check manifest HOME and LEANBACK entries, and verify 32-bit compatibility on TCL Android 11.
4. Verify D-pad, OK, Back, app launching, boot and restore in a non-default-home test first.
5. Only after user approves and rollback is tested, offer HOME selection. Never disable/uninstall stock TCL launcher automatically.

## Acceptance gate
GitHub Actions Build #12 produced a debug APK successfully using the pinned upstream checkout. The updated V2 build still requires CI verification and physical TCL acceptance before any release or Default HOME change. Retain the original system launcher for rollback.
