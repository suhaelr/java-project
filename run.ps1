$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

# Prioritas: paket portable (tanpa butuh Java terinstall)
$portableExe = Join-Path $root "release\SelisihKurang\SelisihKurang.exe"
if (Test-Path $portableExe) {
    & $portableExe
    exit $LASTEXITCODE
}

# Fallback: jar + java dari PATH
$jar = Join-Path $root "dist\selisih-kurang.jar"
if (-not (Test-Path $jar)) {
    & (Join-Path $root "build.ps1")
}
$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    Write-Host "Jalankan package-portable.ps1 dulu untuk membuat versi portable."
    exit 1
}
& $java.Source -jar $jar
