@echo off
rem This example loads a source text file and runs an Anaplan import into a module. 
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc

set AnaplanUser="fred.smith@mycompany.com"
set WorkspaceId="My Workspace"
set ModelId="My Model"
set Operation=-file "My Source.txt" -put "C:\My Source.txt" -import "My Module from My Source.txt" -execute -output "C:\My Errors.txt"

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
cd %~dp0
if not %AnaplanUser% == "" set Credentials=-user %AnaplanUser%
set Command=.\AnaplanClient.bat %Credentials% -workspace %WorkspaceId% -model %ModelId% %Operation%
@echo %Command%
cmd /c %Command%
pause
