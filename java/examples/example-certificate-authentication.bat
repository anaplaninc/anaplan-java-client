@echo off
rem This example lists a user's workspaces

set ServiceLocation="https://api.anaplan.com/"
set Keystore="/path/to/keystore.jks"
set KeystoreAlias="alias"
set KeystorePassword="password"
set Operation="-W"

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
cd %~dp0
set Command=.\AnaplanClient.bat -s %ServiceLocation% -k %Keystore% -ka %KeystoreAlias% -kp %KeystorePassword% %Operation%
@echo %Command%
cmd /c %Command%
pause
