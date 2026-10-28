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

## Physical TCL acceptance before delivery

1. Upgrade the signed V3.1 build to signed V4 with `adb install -r` or TV's
   normal package installer; do not uninstall. Check app order, hidden apps,
   dock, launcher settings and saved wallpaper choices.
2. Navigate all Home cards, settings rows and V4 preview/apply/reset with the
   TCL remote, including Back from the system image picker.
3. Import both a JPG and PNG from a USB drive or TV storage. Check Preview
   leaves the active Home unchanged, Apply updates it and Reset restores the
   prior theme wallpaper. Check behavior when the TV has no file picker.
4. Turn Animations off and confirm navigation remains responsive and focus
   remains visible. Turn it back on and check card movement.
5. Check Home, cold boot and the V3.1 Default Home status on the specified TCL
   firmware. The emulator's HOME role does not guarantee vendor Home routing.
6. Navigate for 30 minutes and inspect memory/temperature responsiveness and
   any redraws after launching/returning from several apps.

No TCL Default Home change, stock app disable or App Store deploy is part of
this QA.
