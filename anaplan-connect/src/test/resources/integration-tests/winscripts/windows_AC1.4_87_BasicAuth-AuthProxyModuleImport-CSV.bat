@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc
call config.bat
set AnaplanUser=%UserCredentials%
set FileName="DepListModuleSmall-TabSingleCol.csv"
set FilePath="data\DepListModuleSmall-TabSingleCol.csv"
set ImportName="DepListModuleSmall from DepListModuleSmall-TabSingleCol.csv"
set DumpFile="dump\DepListModuleSmall from DepListModuleSmall-TabSingleCol.csv"
set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -via %AuthProxyUrl% -viauser %AuthProxyUser% -workspace %WorkspaceId% -model %ModelId% -chunksize 1 -file %FileName% -put %FilePath% -import  %ImportName% -execute -output %DumpFile%

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
cd %~dp0
if not %AnaplanUser% == "" set Credentials=-user %AnaplanUser%
cd ..
set Command=.\AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
