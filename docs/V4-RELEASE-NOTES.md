# Vela TV Launcher 4.0.0 — release notes

## Build identity

- Application ID: `net.velalab.tvlauncher`
- Version: `4.0.0` (`versionCode 40031`)
- Source commit used by the accepted APK: `ea731170d8fc7034f5a1a31fdbcc388e0a67db39`
- CI: [GitHub Actions #31](https://github.com/swansook-cyber/Vela-tv/actions/runs/40706482903) — validate and signed release PASS
- Artifact: [Vela-TV-Launcher-V4-Release](https://github.com/swansook-cyber/Vela-tv/actions/runs/40706482903/artifacts/12824068394)
- APK SHA-256: `4ee2246beef9191e3288f991e4746ca4ca3a16766aaeb9c918f11f6737ca96a2`
- Signing certificate SHA-256: `f3e5a2d996567a1e635f7c674e3f122ff3c6e599af9a0b5aa12bf079c706f904` (the established Vela TV identity)

## Changes

- Rounded Home app cards, a translucent dock and restrained focus animation.
- Three built-in static wallpaper themes with Preview, Apply and Reset.
- JPG/PNG import from the TV using Android's image picker, with an app-private copy and bounded decoding.
- Existing Animations switch turns movement off; app visibility, order, settings and Home behavior remain available.

## Acceptance

The owner reports **TCL Android TV 11 physical QA PASS 9/9** for the accepted
APK, including upgrade over V3.1 without data loss, remote navigation,
wallpaper import, Home from another app, reboot into Vela TV and 30 minutes of
use. See [V4-QA.md](V4-QA.md) for the recorded checks and evidence limits.

## Release tag proposal

Proposed tag: `v4.0.0`, pointing to **`ea731170d8fc7034f5a1a31fdbcc388e0a67db39`**,
the source commit that produced the physically tested APK. The documentation
commit that records acceptance is later and should not be the tag target.
This is a proposal only; no tag or publication has been created.
