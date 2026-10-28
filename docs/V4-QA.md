# Vela TV Launcher V4 QA

V4 retains `net.velalab.tvlauncher`, the pinned Vela TV release certificate, the
existing HOME/LEANBACK entry points, app visibility/order and launcher settings.
The new rounded cards use a restrained 200 ms focus scale by default. The
existing Animations switch disables movement, including card zoom.

## Emulator checks

- AOSP TV API 34, 1280×720: Debug upgrade over V3.1 retained app data.
- D-pad from Launcher settings reached the V4 wallpapers row. OK opened the
  preview screen; its rows accepted D-pad/OK/Back.
- Aurora preview did not change Home until Apply. Apply created a private
  7813-byte file and saved `wallpaper_choice=v4_aurora`.
- This AOSP TV image routes `ACTION_OPEN_DOCUMENT` to a noninteractive
  `DocumentsStub`; V4 falls back to the Android photo picker via
  `ACTION_GET_CONTENT`. A JPG selected there was previewed, copied to private
  storage on Apply and appeared on Home. The picker itself is system UI.
- Imported images must be JPG/PNG, at most 16 MB and 8192 px per side. A failed
  copy leaves the previously applied image intact. Home decodes wallpaper on a
  worker and scales it to the display, using RGB_565 by default.
- A PNG selected from the same picker was also copied and applied. Reset removed
  the wallpaper choice and restored “Theme default”; the existing Animations
  row turned motion off via D-pad and persisted that choice.
- Signed CI APK upgraded V3.1 `31029` to V4 `40030` with `adb install -r`.
  `firstInstallTime` stayed `2026-10-27 22:56:33`, and the saved 7 apps per row
  and 2 rows below the dock remained selected. The release APK also applied
  Aurora through D-pad/OK and showed the saved choice on return to settings.

## Physical TCL acceptance — PASS 9/9

The owner reported these results on 2026-10-28 for TCL Android TV 11,
platform `TCL-AP-MT9221-01`, firmware `V8-T221T05-LF1V202.000657`.
They are owner-reported physical observations; no device log or recording was
provided for independent inspection. The tested build is V4.0.0,
`versionCode 40031`, from GitHub Actions #31 at source commit
`ea731170d8fc7034f5a1a31fdbcc388e0a67db39`.

| # | Physical check | Result |
|---|---|---|
| 1 | Installed over V3.1 | PASS |
| 2 | Home UI and focus | PASS |
| 3 | TCL remote D-pad, OK and Back | PASS |
| 4 | Built-in themes and Reset | PASS |
| 5 | JPG and PNG import | PASS |
| 6 | Show/hide apps and saved settings retained | PASS |
| 7 | Home from another app returns to Vela TV | PASS |
| 8 | Reboot returns to Vela TV | PASS |
| 9 | Continuous 30-minute use | PASS |

The signed APK SHA-256 is
`4ee2246beef9191e3288f991e4746ca4ca3a16766aaeb9c918f11f6737ca96a2`.
This acceptance records the tested firmware only. No stock app was disabled,
and no Vela App Store deployment or main-branch merge was performed.
