@echo off
call config.bat
set AnaplanUser=%UserCredentials%
set ExportName="coresrv-2775-module - tabularSingleCol.csv"
set FilePath="export\basic-auth-coresrv-2775-module-tabularSingleCol.csv"

set Operation=-debug  -service %ServiceUrl% -auth %AuthURL% -workspace %WorkspaceId% -model %ModelId% -export %ExportName% -execute -get %FilePath%

rem *** End of settings - Do not edit below this line ***
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause



