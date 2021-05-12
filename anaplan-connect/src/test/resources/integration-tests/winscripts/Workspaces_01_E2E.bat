@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc
call config.bat

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspaces

set Credentials=-user %UserCredentials%

set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause