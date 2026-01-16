@echo off
setlocal EnableExtensions EnableDelayedExpansion

:: Get current directory of this script
set INSTALL_DIR=%~dp0
set INSTALL_DIR=%INSTALL_DIR:~0,-1%
set EXE_VERSION=%VERSION%

:: Escape backslashes for registry
set REG_INSTALL_DIR=%INSTALL_DIR:\=\\%

echo ========================================
echo Installing CVC Viewer Context Menu
echo Install Path: %INSTALL_DIR%
echo ========================================

set REG_FILE=%TEMP%\CVC_Registry.reg

(
echo Windows Registry Editor Version 5.00
echo.

:: Right-click for all files
echo [HKEY_CLASSES_ROOT\*\shell\Open in CVC Viewer]
echo @="Open in CVC Viewer"
echo "Icon"="\"%REG_INSTALL_DIR%\\icons\\cvc-logo.ico\""
echo.

echo [HKEY_CLASSES_ROOT\*\shell\Open in CVC Viewer\command]
echo @="\"%REG_INSTALL_DIR%\\CVC-Viewer-%EXE_VERSION%.exe\" \"%%1\""
echo.

:: Register .cvcert
echo [HKEY_CLASSES_ROOT\.cvcert]
echo @="CVCViewer.File"
echo.

:: Register .cvreq
echo [HKEY_CLASSES_ROOT\.cvreq]
echo @="CVCViewer.File"
echo.

:: File type handler
echo [HKEY_CLASSES_ROOT\CVCViewer.File]
echo @="CVC Certificate"
echo "Icon"="\"%REG_INSTALL_DIR%\\icons\\cvc-logo.ico\""
echo.

echo [HKEY_CLASSES_ROOT\CVCViewer.File\shell\open\command]
echo @="\"%REG_INSTALL_DIR%\\CVC-Viewer-%EXE_VERSION%.exe\" \"%%1\""
) > "%REG_FILE%"

:: Install registry
regedit /s "%REG_FILE%"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Registry installation failed.
    echo Please run as Administrator.
    pause
    exit /b 1
)

echo.
echo ========================================
echo CVC Viewer registered successfully!
echo Right-click any file or .cvcert/.cvreq
echo ========================================
pause
