# Buat paket portable siap kirim ke laptop rekan (tanpa install Java / tanpa build)
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Find-JavaTool([string]$name) {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\$name.exe"))) {
        return (Join-Path $env:JAVA_HOME "bin\$name.exe")
    }
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $fallbacks = @(
        "C:\Program Files\Java\jdk-26.0.1\bin\$name.exe",
        "C:\Program Files\Java\latest\bin\$name.exe"
    )
    foreach ($path in $fallbacks) {
        if (Test-Path $path) { return $path }
    }
    throw "Tidak menemukan $name. Install JDK 17+ atau set JAVA_HOME."
}

Write-Host "==> Compile aplikasi..."
& (Join-Path $root "build.ps1")

$jpackage = Find-JavaTool "jpackage"
$releaseDir = Join-Path $root "release"
$appDir = Join-Path $releaseDir "SelisihKurang"

if (Test-Path $appDir) { Remove-Item $appDir -Recurse -Force }
New-Item -ItemType Directory -Path $releaseDir -Force | Out-Null

Write-Host "==> Membuat paket portable (bundled JRE, ~1 menit)..."
& $jpackage `
    --input (Join-Path $root "dist") `
    --name SelisihKurang `
    --main-jar selisih-kurang.jar `
    --main-class com.selisihkurang.SelisihKurangApp `
    --type app-image `
    --dest $releaseDir `
    --app-version 1.0.0 `
    --description "Rekonsiliasi RC vs EJ - Selisih Kurang" `
    --vendor "SelisihKurang" `
    --copyright "2026"

# Salin sample data ke dalam paket agar rekan bisa langsung uji coba
$samplesDest = Join-Path $appDir "samples"
Copy-Item -Path (Join-Path $root "samples") -Destination $samplesDest -Recurse -Force

$readme = @"
SELISIH KURANG - Rekonsiliasi RC vs EJ
======================================

CARA PAKAI (tanpa install apapun):
  1. Double-click: SelisihKurang.exe
  2. Browse File RC dan File EJ (contoh ada di folder samples\)
  3. Klik Rekon
  4. Lihat hasil di tab Match / Nasabah Diuntungkan / ACQ
  5. Tab Berita Acara -> Print Berita Acara

Tidak perlu install Java. Tidak perlu build.
Cukup copy seluruh folder ini ke laptop manapun (Windows 64-bit).

Folder samples\ berisi data contoh untuk dicoba.
"@
Set-Content -Path (Join-Path $appDir "CARA_PAKAI.txt") -Value $readme -Encoding UTF8

$zipPath = Join-Path $releaseDir "SelisihKurang-portable.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
Compress-Archive -Path $appDir -DestinationPath $zipPath -Force

Write-Host ""
Write-Host "SELESAI!"
Write-Host "  Folder : $appDir"
Write-Host "  Zip    : $zipPath"
Write-Host ""
Write-Host "Kirim file ZIP ke rekan. Extract lalu double-click SelisihKurang.exe"
