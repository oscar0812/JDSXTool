@echo off
setlocal

:: Check for version argument
if "%1"=="" (
    echo Usage: release.bat ^<version^>
    exit /b 1
)

set VERSION=%1

:: Stage all uncommitted changes
git add .

:: Commit changes
set /p COMMIT_MSG=Enter a commit message:
if "%COMMIT_MSG%"=="" set COMMIT_MSG=Release version %VERSION%

git commit -m "%COMMIT_MSG%"

:: Update version in build.gradle
(for /f "delims=" %%i in (build.gradle) do (
    echo %%i | findstr /r "version = '.*'" >nul
    if errorlevel 1 (
        echo %%i
    ) else (
        echo version = '%VERSION%'
    )
)) > temp_build.gradle

move /y temp_build.gradle build.gradle >nul

:: Commit version change
git add build.gradle
git commit -m "Bump version to %VERSION%"

:: Tag the release
git tag %VERSION%

:: Push commits and tag
git push origin main
git push origin %VERSION%

echo Successfully pushed release %VERSION% to GitHub!
