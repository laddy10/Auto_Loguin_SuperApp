@echo off
echo.
echo [VERIFICACION OLLAMA]
echo.

REM Verificar si Ollama está corriendo
curl -s http://localhost:11434/api/tags > nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ Ollama está corriendo
    echo.
) else (
    echo ✗ ERROR: Ollama NO está corriendo
    echo.
    echo Soluciones:
    echo   1. Abrir una terminal nueva y ejecutar: ollama serve
    echo   2. En otra terminal: ollama run mistral
    echo.
    pause
    exit /b 1
)

REM Verificar modelo mistral
ollama list | findstr "mistral" > nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ Modelo 'mistral' disponible
    echo.
) else (
    echo ✗ ADVERTENCIA: Modelo 'mistral' no encontrado
    echo   Ejecutar: ollama pull mistral
    echo.
)

echo [OK] Ollama configurado correctamente
echo.
pause