@echo off
title SafeSen-AI: Instant Public Link Generator
echo =====================================================================
echo    SafeSen-AI: Instant Public Web Link Generator (For Viva Demo)
echo =====================================================================
echo.
echo 1. Ensure your Spring Boot server is running on port 8080 (via run.bat).
echo 2. Generating your instant public HTTPS URL...
echo.
echo You can share this public HTTPS URL with anyone on mobile, laptop, or
echo external evaluators during your presentation!
echo.
npx localtunnel --port 8080
pause

