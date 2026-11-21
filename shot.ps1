# Sends key events to the device, then saves both screens as shot_top.png / shot_bot.png.
# Usage: powershell -File shot.ps1 [-Keys "KEYCODE_DPAD_RIGHT KEYCODE_BUTTON_A"] [-Wait 3]
param([string]$Keys = '', [int]$Wait = 3)
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$cmd = 'input keyevent KEYCODE_WAKEUP; sleep 1; '
foreach ($k in ($Keys -split '\s+' | Where-Object { $_ })) { $cmd += "input keyevent $k; sleep 0.4; " }
$cmd += "sleep $Wait; screencap -p -d 4630947039571902866 /sdcard/Download/duo_top.png; screencap -p -d 4630946671160541331 /sdcard/Download/duo_bot.png"
& $adb shell $cmd
& $adb pull /sdcard/Download/duo_top.png "$PSScriptRoot\shot_top.png" 2>&1 | Out-Null
& $adb pull /sdcard/Download/duo_bot.png "$PSScriptRoot\shot_bot.png" 2>&1 | Out-Null
