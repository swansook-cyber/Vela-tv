# Vela TV Launcher V3 — Acceptance Record

Date: 2026-10-27
Status: **ACCEPTED on physical TCL TV (user-reported)**
Repository: `swansook-cyber/Vela-tv`
Accepted branch: `develop/vela-tv-launcher`
Accepted source commit: `9c03e06778b6cd22c461b58dfe75a5d55ccf615b`
Version name: `3.0.0`
Version code: `30028`
Application ID: `net.velalab.tvlauncher`

## Build provenance
- GitHub Actions runs [#27](https://github.com/swansook-cyber/Vela-tv/actions/runs/40535470105) and [#28](https://github.com/swansook-cyber/Vela-tv/actions/runs/40535482227): validation and release reported PASS.
- APK SHA-256: `9453895663dba0a44092e45b43630e8ef8cf0211ff0a8e9a0ef260b5cbeda625`
- Release signing certificate SHA-256: `f3e5a2d996567a1e635f7c674e3f122ff3c6e599af9a0b5aa12bf079c706f904`
- Signing alias: `vela-tv-launcher`. Retain the same signing identity for all subsequent releases.

## Physical TCL acceptance — 7/7 PASS (user confirmed)
1. Version 30028 installs and launches.
2. Approved original logo displays correctly without distortion.
3. TV banner displays correctly.
4. Bottom app rows are not clipped.
5. TCL remote direction keys, OK, and Back work.
6. Home app visibility can be toggled.
7. Settings persist after restarting the TV.

## Additional evidence and limits
- Android TV emulator upgrade test: signed build 30027 upgraded in-place to 30028 using `adb install -r`; the seven-apps-per-row and two-rows-below-dock settings persisted.
- Physical TCL acceptance confirms the seven listed behaviors, but does **not** separately establish a 30028-to-future-version in-place upgrade on TCL.
- Prior `docs/V3-LAYOUT-QA.md` describes an earlier test stage; its TCL NOT TESTED wording was correct at that time and is superseded for the seven acceptance items by this record.
- User acceptance is based on the user's physical-device report, not an independently observed test session.

## Release controls
- **Do not merge to `main`** without separate authorization.
- **Do not deploy or publish to Vela App Store** without separate authorization.
- Do not change TCL default Home or disable system apps.
- Do not rotate signing key automatically; block release if alias or certificate differs.
- Freeze V3 as accepted baseline; prioritize 3–7 days of real-world stability monitoring before feature expansion.
- Any V3.1 work belongs on a separate branch and requires its own validation and device QA.
