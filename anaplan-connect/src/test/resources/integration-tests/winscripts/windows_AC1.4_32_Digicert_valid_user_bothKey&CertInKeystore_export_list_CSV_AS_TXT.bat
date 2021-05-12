@echo off
call config.bat
set ExportName="Grid - coresrv-2779-list.csv"
set Operation=-debug   -service %ServiceUrl% -auth %AuthURL% -workspace %WorkspaceId% -model %ModelID% -export %ExportName% -execute -get "export\cert-auth-Grid-coresrv-2779-list-csv.txt"

rem *** End of settings - Do not edit below this line ***

set Credentials=-k %KeyStorePath% -ka %KeyStoreAlias% -kp %KeyStorePass%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
