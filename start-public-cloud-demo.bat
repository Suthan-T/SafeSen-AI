@echo off
title SafeSen-AI: Live Public Cloud Server
echo =====================================================================
echo    SafeSen-AI: Women Safety Analytics Platform
echo    Dept. of Computer Science and Engineering, V.S.B. Engineering College
echo =====================================================================
echo.
echo Starting Spring Boot Server on http://localhost:8080 ...
start /B java -jar target\women-safety-analytics-1.0.0.jar
timeout /t 8 /nobreak >nul

echo.
echo =====================================================================
echo Launching Cloudflare High-Speed Public HTTPS Tunnel...
echo =====================================================================
echo.
.\cloudflared.exe tunnel --url http://localhost:8080
pause
