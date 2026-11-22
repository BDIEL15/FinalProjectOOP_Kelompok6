@echo off
setlocal
cd /d "%~dp0"
java -Dmars.pythonDir="%~dp0runtime\python" -jar "%~dp0MarsReboot-1.0.0.jar"
if errorlevel 1 pause
endlocal
