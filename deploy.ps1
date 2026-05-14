$adb = "$env:USERPROFILE\AppData\Local\Android\Sdk\platform-tools\platform-tools\adb.exe"
$apk = "$PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk"
$javaHome = "C:\Program Files\Android\Android Studio\jbr"

Write-Host "Building..." -ForegroundColor Cyan
$env:JAVA_HOME = $javaHome
& "$PSScriptRoot\gradlew.bat" assembleDebug
if ($LASTEXITCODE -ne 0) { Write-Host "Build failed." -ForegroundColor Red; exit 1 }

Write-Host "Installing on device..." -ForegroundColor Cyan
& $adb -s 192.168.5.185:37861 install -r $apk
if ($LASTEXITCODE -eq 0) {
    Write-Host "Done! App updated on phone." -ForegroundColor Green
} else {
    Write-Host "Install failed. Is the phone on the same WiFi and ADB connected?" -ForegroundColor Red
}
