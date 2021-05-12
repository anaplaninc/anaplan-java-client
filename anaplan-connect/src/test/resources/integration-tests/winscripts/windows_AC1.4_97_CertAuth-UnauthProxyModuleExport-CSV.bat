@echo off
call config.bat

set ExportName="coresrv-2779-module - multicol.csv"
set FilePath="export\certauth-coresrv-2779-module - multicol.csv"

set Operation=-debug -auth %AuthURL% -via %UnAuthProxyUrl% -workspace %WorkspaceId% -model %ModelID% -export %ExportName% -execute -get %FilePath%

rem *** End of settings - Do not edit below this line ***
set Credentials=""-k %KeyStorePath% -ka %KeyStoreAlias% -kp %KeyStorePass%""
set Command=AnaplanClient.bat %Credentials% %Operation%@echo %Command%set Command=.\AnaplanClient.bat -s %ServiceUrl% -k %KeyStorePath% -ka %KeyStoreAlias% -kp %KeyStorePass% %Operation%
@echo %Command%
cmd /c %Command%
pause
