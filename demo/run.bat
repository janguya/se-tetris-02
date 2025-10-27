@echo off
setlocal
cd /d %~dp0

REM 현재 java 버전 확인
for /f "tokens=2 delims==" %%v in ('java -XshowSettings:properties -version 2^>^&1 ^| find "java.version"') do set JAVAVER=%%v
echo 현재 Java 버전: %JAVAVER%

REM 8이면 내장 JavaFX 사용
echo %JAVAVER% | find "1.8" >nul
if %errorlevel%==0 (
    echo Java 8 detected. Running with built-in JavaFX...
    javaw -jar target\tetris-1.0.jar
    goto :end
)

REM 11 이상이면 OpenJFX SDK 사용
echo Java 11+ detected. Running with external JavaFX SDK...
set FXPATH=javafx-sdk\lib
java --module-path "%FXPATH%" --add-modules javafx.controls,javafx.fxml -jar target\tetris-1.0.jar

:end
pause
