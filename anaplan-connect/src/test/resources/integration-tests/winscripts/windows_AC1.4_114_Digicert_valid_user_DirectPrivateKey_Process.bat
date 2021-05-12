@echo off
call config.bat

set ImportDataSource1="coresrv-2776-list.csv"
set ImportFileName1="data\coresrv-2776-list.csv"
set ImportDataSource2="coresrv-2779-module.csv"
set ImportFileName2="data\coresrv-2779-module.csv"
set ErrorDump="dump\basic-auth-Process_errors"
set ExportName1="coresrv-2779-module - multicol.csv"
set ExportName2="coresrv-2775-module - tabularSingleCol.csv"
set FileName1="export\basic-auth-process-coresrv-2779-module - multicol.csv"
set FileName2="export\basic-auth-process-coresrv-2775-module - tabularSingleCol.csv"

set Operation=-debug -certificate %CACertPath% -pkey %PrivateKey% -s %ServiceUrl% -auth %AuthURL% -workspace %WorkspaceId% -model %ModelID% -file %ImportDataSource1% -put %ImportFileName1% -file %ImportDataSource2% -put %ImportFileName2%  -process %ProcessName% -execute -file %ExportName1% -get %FileName1% -file %ExportName2%  -get %FileName2% -output %ErrorDump%"


rem *** End of settings - Do not edit below this line ***
setlocal enableextensions enabledelayedexpansion || exit /b 1
cd %~dp0
cd ..
set Command=.\AnaplanClient.bat  %Operation%
@echo %Command%
cmd /c %Command%
pause
