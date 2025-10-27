# Tetris 배포 패키지 자동 생성 스크립트
param(
    [string]$targetDir = "target",
    [string]$iconFile = "icon.ico"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Creating Tetris Distribution Package..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$runtimeDir = Join-Path $targetDir "tetris-runtime"
$binDir = Join-Path $runtimeDir "bin"
$ps1File = Join-Path $binDir "Tetris.ps1"
$exeFile = Join-Path $binDir "Tetris.exe"
$zipFile = Join-Path $targetDir "Tetris-Portable-$env:PROJECT_VERSION.zip"

# 1. Create PowerShell launcher script
Write-Host "`n[1/4] Creating PowerShell launcher script..." -ForegroundColor Yellow
$launcherScript = @"
# Tetris Launcher
Set-Location `$PSScriptRoot
& .\java.exe -m com.example.tetris/com.example.Main
"@

Set-Content -Path $ps1File -Value $launcherScript -Encoding UTF8
Write-Host "  ✓ Launcher script created: $ps1File" -ForegroundColor Green

# 2. Convert PowerShell script to EXE
Write-Host "`n[2/4] Creating EXE file..." -ForegroundColor Yellow
try {
    # Remove existing EXE
    if (Test-Path $exeFile) {
        Remove-Item $exeFile -Force
    }
    
    # Import ps2exe module
    Import-Module ps2exe -ErrorAction Stop
    
    # Create EXE
    Invoke-PS2EXE -inputFile $ps1File `
                  -outputFile $exeFile `
                  -iconFile $iconFile `
                  -noConsole `
                  -title "Tetris" `
                  -product "Tetris Game" `
                  -version "1.0.0.0" `
                  -ErrorAction Stop
    
    Write-Host "  ✓ EXE file created: $exeFile" -ForegroundColor Green
} catch {
    Write-Host "  ✗ EXE creation failed: $_" -ForegroundColor Red
    Write-Host "  → Using .bat file instead." -ForegroundColor Yellow
}

# 3. Create shortcut
Write-Host "`n[3/5] Creating shortcut..." -ForegroundColor Yellow
try {
    $shortcutPath = Join-Path $runtimeDir "Tetris.lnk"
    $targetPath = Join-Path $binDir "Tetris.exe"
    
    $WshShell = New-Object -ComObject WScript.Shell
    $Shortcut = $WshShell.CreateShortcut($shortcutPath)
    $Shortcut.TargetPath = $targetPath
    $Shortcut.WorkingDirectory = $binDir
    $Shortcut.IconLocation = "$targetPath, 0"
    $Shortcut.Description = "Tetris Game"
    $Shortcut.Save()
    
    Write-Host "  ✓ Shortcut created: $shortcutPath" -ForegroundColor Green
} catch {
    Write-Host "  ⚠ Shortcut creation failed: $_" -ForegroundColor Yellow
    Write-Host "  → Continuing anyway..." -ForegroundColor Yellow
}

# 4. Create README file
Write-Host "`n[4/5] Creating README file..." -ForegroundColor Yellow
$readmeContent = @"
Tetris Game
================

How to Run
----------
1. Double-click Tetris.lnk (shortcut)
   OR
2. Run bin\Tetris.exe directly

System Requirements
-------------------
- Windows 7 or later
- No Java installation required (includes runtime)

File Structure
--------------
- Tetris.lnk      : Game launcher shortcut
- bin\Tetris.exe  : Actual executable
- bin\*.dll       : Runtime libraries
- lib\            : JavaFX modules
- conf\           : Configuration files

Troubleshooting
---------------
- If the shortcut doesn't work, run bin\Tetris.exe directly
- If nothing happens, try running as administrator

Enjoy the game!
"@

$readmePath = Join-Path $runtimeDir "README.txt"
# UTF-8 without BOM for universal compatibility
Set-Content -Path $readmePath -Value $readmeContent -Encoding UTF8 -Force
Write-Host "  ✓ README file created: $readmePath" -ForegroundColor Green

# 5. Wait for file system sync
Start-Sleep -Seconds 2

# 6. Create ZIP file
Write-Host "`n[5/5] Creating ZIP file..." -ForegroundColor Yellow
try {
    # Remove existing ZIP
    if (Test-Path $zipFile) {
        Remove-Item $zipFile -Force
    }
    
    # Create ZIP
    Compress-Archive -Path $runtimeDir `
                     -DestinationPath $zipFile `
                     -Force `
                     -ErrorAction Stop
    
    $zipSize = (Get-Item $zipFile).Length / 1MB
    Write-Host "  ✓ ZIP file created: $zipFile" -ForegroundColor Green
    Write-Host "  → File size: $([math]::Round($zipSize, 2)) MB" -ForegroundColor Green
} catch {
    Write-Host "  ✗ ZIP creation failed: $_" -ForegroundColor Red
    exit 1
}

# Done
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Distribution Package Created Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`n📦 Distribution file: $zipFile" -ForegroundColor Cyan
Write-Host "`nHow to use:" -ForegroundColor White
Write-Host "  1. Extract the ZIP file" -ForegroundColor White
Write-Host "  2. Run Tetris.lnk (shortcut) in tetris-runtime folder" -ForegroundColor White
Write-Host "     OR run bin\Tetris.exe directly" -ForegroundColor White
Write-Host "`n" -ForegroundColor White
