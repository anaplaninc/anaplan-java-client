@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc

call config.bat
set AnaplanUser=%UserCredentials%

set FilePath="list-data/list-items.json"
set ChunkSize=1

set ListId="101000000100"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceName% -model %ModelName% -list %ListId% -execute -get:json %FilePath%

#____________________________ Do not edit below this line ______________________________

Credentials="-user %UserCredentials%"

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause