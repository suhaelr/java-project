$ErrorActionPreference = "Stop"

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

$javac = Find-JavaTool "javac"
$jar = Find-JavaTool "jar"

$root = $PSScriptRoot
$classes = Join-Path $root "build\classes"
$dist = Join-Path $root "dist"

if (Test-Path $classes) { Remove-Item $classes -Recurse -Force }
New-Item -ItemType Directory -Path $classes -Force | Out-Null
New-Item -ItemType Directory -Path $dist -Force | Out-Null

$sources = Get-ChildItem -Path (Join-Path $root "src\main\java") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
& $javac -encoding UTF-8 -d $classes @sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$manifest = Join-Path $classes "MANIFEST.MF"
@"
Manifest-Version: 1.0
Main-Class: com.selisihkurang.SelisihKurangApp
"@ | Set-Content -Path $manifest -Encoding ASCII

$jarFile = Join-Path $dist "selisih-kurang.jar"
if (Test-Path $jarFile) { Remove-Item $jarFile -Force }
Push-Location $classes
& $jar cfm $jarFile MANIFEST.MF com
Pop-Location

Write-Host "Build sukses: $jarFile"
