param(
    [Parameter(Mandatory = $true)][string]$Serial,
    [Parameter(Mandatory = $true)][string]$Adb,
    [string]$Apk,
    [string]$ExpectedNotSet = 'Not set as TV home'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($Serial -notmatch '^emulator-\d+$') { throw 'Use an Android TV emulator serial only.' }
if ((& $Adb -s $Serial shell getprop ro.kernel.qemu).Trim() -ne '1') {
    throw 'Target is not an emulator.'
}
if ($Apk) {
    $install = & $Adb -s $Serial install -r $Apk
    if ($LASTEXITCODE -ne 0 -or $install -notcontains 'Success') { throw 'APK install failed.' }
}

function Get-Ui {
    & $Adb -s $Serial shell uiautomator dump /sdcard/vela-home-test.xml 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'UI dump failed.' }
    return [xml]((& $Adb -s $Serial exec-out cat /sdcard/vela-home-test.xml) -join "`n")
}
function Get-Node($ui, [string]$id) {
    return $ui.SelectSingleNode("//*[@resource-id='$id']")
}
function Assert-NotSet($ui) {
    $node = Get-Node $ui 'net.velalab.tvlauncher:id/defaultHomeStatus'
    if (-not $node -or $node.GetAttribute('text') -ne $ExpectedNotSet) {
        throw 'Home status incorrectly reports success after cancellation.'
    }
}

& $Adb -s $Serial shell am start -n net.velalab.tvlauncher/com.gathin.tvhome.MainActivity `
    -a com.gathin.tvhome.action.LAUNCHER_SETTINGS | Out-Null
$ui = Get-Ui
Assert-NotSet $ui
$target = 'net.velalab.tvlauncher:id/defaultHomeChoice'
for ($step = 0; $step -lt 20; $step++) {
    $focused = $ui.SelectSingleNode('//*[@focused="true"]')
    if ($focused -and $focused.GetAttribute('resource-id') -eq $target) { break }
    & $Adb -s $Serial shell input keyevent KEYCODE_DPAD_DOWN | Out-Null
    $ui = Get-Ui
}
$focused = $ui.SelectSingleNode('//*[@focused="true"]')
if (-not $focused -or $focused.GetAttribute('resource-id') -ne $target) {
    throw 'D-pad did not reach Vela Home setup row.'
}
& $Adb -s $Serial shell input keyevent KEYCODE_DPAD_CENTER | Out-Null
$ui = Get-Ui
if (-not (Get-Node $ui 'com.android.permissioncontroller:id/list')) {
    throw 'Android HOME role dialog did not open.'
}
& $Adb -s $Serial shell input keyevent KEYCODE_BACK | Out-Null
$ui = Get-Ui
Assert-NotSet $ui
if ($ui.SelectSingleNode('//*[@focused="true"]').GetAttribute('resource-id') -ne $target) {
    throw 'Focus did not return to Vela Home setup row.'
}
& $Adb -s $Serial shell input keyevent KEYCODE_DPAD_DOWN | Out-Null
$ui = Get-Ui
if ($ui.SelectSingleNode('//*[@focused="true"]').GetAttribute('resource-id') -ne
        'net.velalab.tvlauncher:id/openHomeSettingsChoice') {
    throw 'D-pad did not reach system Home settings row.'
}
& $Adb -s $Serial shell input keyevent KEYCODE_DPAD_CENTER | Out-Null
$ui = Get-Ui
if (-not $ui.SelectSingleNode('//*[@text="Vela TV Launcher" and @package!="net.velalab.tvlauncher"]')) {
    throw 'System Home settings did not open.'
}
& $Adb -s $Serial shell input keyevent KEYCODE_BACK | Out-Null
$ui = Get-Ui
Assert-NotSet $ui
Write-Output 'PASS: D-pad focus, OK, denied HOME role, Back, system Home settings, and return status'
