# Vela TV Launcher release signing

The permanent release identity for `net.velalab.tvlauncher` is the alias
`vela-tv-launcher`, RSA 3072, with certificate SHA-256
`f3e5a2d996567a1e635f7c674e3f122ff3c6e599af9a0b5aa12bf079c706f904`.
The certificate was created on 2026-10-27 with a 10,000-day validity period.

The original keystore is outside this repository at
`D:\Project AI\Vela Signing\VelaTV\vela-tv-launcher-release.jks`.
A separate backup is in the owner's Google Drive folder
`Vela TV Launcher Signing Backup`. The store and key passwords are separate
Windows Credential Manager entries for the owner account:
`VelaTVLauncher/ReleaseStorePassword` and
`VelaTVLauncher/ReleaseKeyPassword`.

GitHub Actions uses the four repository secrets
`VELA_RELEASE_KEYSTORE_BASE64`, `VELA_RELEASE_STORE_PASSWORD`,
`VELA_RELEASE_KEY_PASSWORD`, and `VELA_RELEASE_KEY_ALIAS`. The workflow
requires the alias and compares the keystore certificate and signed APK
certificate to `.github/vela-release-cert.sha256`. A missing key, missing
secret, or certificate mismatch fails the release. Never generate a replacement
key automatically. Recover the same keystore and credentials before any
future release.

The first release signed with this identity establishes its upgrade lineage.
An APK signed by a different certificate cannot be installed over it as a
normal update. Keep the package ID and increment `versionCode` for each release.
