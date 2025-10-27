@echo off
cd /d %~dp0
rmdir /s /q Tetris >nul 2>&1

echo [1/3] Packaging with Maven...
call mvn clean package

echo [2/3] Creating runtime (with JavaFX included)...
jlink ^
--module-path "%JAVA_HOME%\jmods;javafx-sdk\lib" ^
--add-modules java.base,javafx.controls,javafx.fxml ^
--output custom-runtime

echo [3/3] Building app-image using custom runtime...
jpackage ^
--dest Tetris ^
--input target ^
--main-jar tetris-1.0.jar ^
--main-class com.example.Main ^
--name "Tetris" ^
--type exe ^
--runtime-image custom-runtime ^
--icon icon.ico ^
--java-options "-Dprism.order=sw -Dprism.verbose=true -Dprism.d3d=false -Dprism.forceGPU=false -Dprism.allowhidpi=false"

echo Done. Check Tetris\Tetris.exe
pause
