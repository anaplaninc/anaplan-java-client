@echo off

call config.bat
echo %KeyStorePath%
set ActionName="delete from list"


set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId% -action %ActionName% -execute"
echo %Operation%
rem *** End of settings - Do not edit below this line ***
@echo %Operation%
set Credentials=-k %KeyStorePath% -ka %KeyStoreAlias% -kp %KeyStorePass%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
