@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc
call config.bat

set ProxyUser=nrajanna:Welcome2
set FileName="DepListModuleSmall-TabSingleCol.csv"
set FilePath="data\DepListModuleSmall-TabSingleCol.csv"
set ImportName="DepListModuleSmall from DepListModuleSmall-TabSingleCol.csv"
set DumpFile="dump\DepListModuleSmall from DepListModuleSmall-TabSingleCol.csv"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -via %AuthProxyUrl% -viauser %ProxyUser% -workspace %WorkspaceId% -model %ModelId% -chunksize 1 -file %FileName% -put %FilePath% -import %ImportName% -execute -output %DumpFile%

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
