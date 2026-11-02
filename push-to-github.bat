@echo off
title SafeSen-AI: Push to GitHub
echo =====================================================================
echo    SafeSen-AI: Push Project to GitHub (Suthan-T & Tamiliniyan77)
echo =====================================================================
echo.
echo Make sure you have created the empty repository on GitHub:
echo https://github.com/new -> Repository Name: SafeSen-AI
echo.
echo Pushing local branch 'main' to https://github.com/Suthan-T/SafeSen-AI.git ...
git push -u origin main
echo.
if %ERRORLEVEL% equ 0 (
    echo =====================================================================
    echo [SUCCESS] Project successfully uploaded to:
    echo https://github.com/Suthan-T/SafeSen-AI
    echo.
    echo Now add your friend as a collaborator:
    echo https://github.com/Suthan-T/SafeSen-AI/settings/access -> Add "Tamiliniyan77"
    echo =====================================================================
) else (
    echo =====================================================================
    echo [NOTICE] If the repository does not exist yet, please create it at:
    echo https://github.com/new (Name: SafeSen-AI)
    echo Then run this file again!
    echo =====================================================================
)
echo.
pause

