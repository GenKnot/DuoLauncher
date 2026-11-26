# Builds DuoLauncher.apk with the plain SDK tools (no Gradle): aapt2 -> javac -> d8 -> zipalign -> apksigner.
# Usage: powershell -File build.ps1 [-Install]
param([switch]$Install)
$ErrorActionPreference = 'Stop'
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$bt = "$sdk\build-tools\35.0.0"
$jar = "$sdk\platforms\android-35\android.jar"
$jdk = 'C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:JAVA_HOME = $jdk
$root = $PSScriptRoot
$out = "$root\build"

if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory -Force "$out\classes", "$out\dex" | Out-Null

& "$bt\aapt2.exe" compile --dir "$root\res" -o "$out\res.zip"
if ($LASTEXITCODE) { throw 'aapt2 compile failed' }
& "$bt\aapt2.exe" link -o "$out\base.apk" -I $jar --manifest "$root\AndroidManifest.xml" -R "$out\res.zip" --auto-add-overlay `
    --min-sdk-version 30 --target-sdk-version 35 --version-code 161 --version-name 1.6.1
if ($LASTEXITCODE) { throw 'aapt2 link failed' }

$srcs = Get-ChildItem "$root\src" -Recurse -Filter *.java | ForEach-Object FullName
& "$jdk\bin\javac.exe" -encoding UTF-8 -source 11 -target 11 -Xlint:-options -nowarn -classpath $jar -d "$out\classes" @srcs
if ($LASTEXITCODE) { throw 'javac failed' }

$classes = Get-ChildItem "$out\classes" -Recurse -Filter *.class | ForEach-Object FullName
& "$bt\d8.bat" --release --min-api 30 --lib $jar --output "$out\dex" @classes
if ($LASTEXITCODE) { throw 'd8 failed' }

Copy-Item "$out\base.apk" "$out\unsigned.apk"
Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
$z = [IO.Compression.ZipFile]::Open("$out\unsigned.apk", 'Update')
[void][IO.Compression.ZipFileExtensions]::CreateEntryFromFile($z, "$out\dex\classes.dex", 'classes.dex')
# assets added here rather than with aapt2 -A, which writes Windows backslashes into entry names
foreach ($f in Get-ChildItem "$root\assets" -Recurse -File) {
    $name = 'assets/' + $f.FullName.Substring("$root\assets\".Length).Replace('\', '/')
    [void][IO.Compression.ZipFileExtensions]::CreateEntryFromFile($z, $f.FullName, $name, 'NoCompression')
}
$z.Dispose()

& "$bt\zipalign.exe" -f -p 4 "$out\unsigned.apk" "$out\aligned.apk"
if ($LASTEXITCODE) { throw 'zipalign failed' }

$ks = "$root\debug.keystore"
if (-not (Test-Path $ks)) {
    & "$jdk\bin\keytool.exe" -genkeypair -keystore $ks -storepass android -keypass android -alias duo `
        -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Duo Launcher'
}
& "$bt\apksigner.bat" sign --ks $ks --ks-pass pass:android --key-pass pass:android --out "$root\DuoLauncher.apk" "$out\aligned.apk"
if ($LASTEXITCODE) { throw 'apksigner failed' }
"built $root\DuoLauncher.apk ($((Get-Item "$root\DuoLauncher.apk").Length) bytes)"

if ($Install) {
    $adb = "$sdk\platform-tools\adb.exe"
    & $adb install -r "$root\DuoLauncher.apk"
    & $adb shell appops set --uid com.duo.launcher MANAGE_EXTERNAL_STORAGE allow
    & $adb shell am start -n com.duo.launcher/.MainActivity
}
