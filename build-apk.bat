@echo off
gradle --no-daemon assembleDebug
if errorlevel 1 exit /b %errorlevel%
echo APK: app\build\outputs\apk\debug\app-debug.apk
