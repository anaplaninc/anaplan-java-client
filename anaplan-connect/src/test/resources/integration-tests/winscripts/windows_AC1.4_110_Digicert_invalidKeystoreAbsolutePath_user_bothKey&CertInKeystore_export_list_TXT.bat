@echo off
call config.bat
set ExportName="Grid - LargeList.txt"
set FilePath="exports\cert-auth-Grid-LargeList.txt"

set Operation=-debug  -service %ServiceUrl% -auth %AuthURL% -workspace %WorkspaceId% -model %ModelID% -export %ExportName% -execute -get %FilePath%

rem *** End of settings - Do not edit below this line ***
set Credentials=-k %KeyStorePath% -ka %KeyStoreAlias% -kp %KeyStorePass%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
