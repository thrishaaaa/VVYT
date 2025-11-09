@echo off
echo Legal Mediation Chatbot
echo =====================
echo.
echo Starting the chatbot...
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo Error: Python is not installed or not in PATH
    echo Please install Python 3.7+ and try again
    pause
    exit /b 1
)

REM Check if requirements are installed
echo Checking dependencies...
pip show ollama >nul 2>&1
if errorlevel 1 (
    echo Installing dependencies...
    pip install -r requirements.txt
    if errorlevel 1 (
        echo Error: Failed to install dependencies
        pause
        exit /b 1
    )
)

echo.
echo Dependencies installed successfully!
echo.
echo Choose an option:
echo 1. Run example usage script
echo 2. Start API server
echo 3. Start Web UI (Chat Interface) - Flask
echo 4. Start Simple Web UI (Chat Interface) - No Dependencies
echo 5. Start Integrated Web UI (Chat + Documents) - No Dependencies
echo 6. Run tests
echo 7. Exit
echo.

set /p choice="Enter your choice (1-7): "

if "%choice%"=="1" (
    echo Starting example usage script...
    python example_usage.py
) else if "%choice%"=="2" (
    echo Starting API server...
    python api_wrapper.py
) else if "%choice%"=="3" (
    echo Starting Flask Web UI...
    echo.
    echo Opening web interface at: http://localhost:5000
    echo Press Ctrl+C to stop the server
    echo.
    python web_ui.py
) else if "%choice%"=="4" (
    echo Starting Simple Web UI (No Flask required)...
    echo.
    echo Opening web interface at: http://localhost:5000
    echo Press Ctrl+C to stop the server
    echo.
    python simple_web_ui.py
) else if "%choice%"=="5" (
    echo Starting Integrated Web UI (Chat + Documents)...
    echo.
    echo Opening integrated web interface at: http://localhost:5000
    echo Features: Chat Assistant + Document Management
    echo Press Ctrl+C to stop the server
    echo.
    python integrated_web_ui.py
) else if "%choice%"=="6" (
    echo Running tests...
    python test_chatbot.py
) else if "%choice%"=="7" (
    echo Goodbye!
) else (
    echo Invalid choice. Please run the script again.
)

pause 