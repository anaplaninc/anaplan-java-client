@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc

call config.bat
set AnaplanUser=%UserCredentials%
set FilePath="complex-2-pages-2rows-1cols3Pages_dateAsCols2.csv"
set ModuleName="complex-2-pages-2rows-1cols3Pages"
set ViewName="dateAsCols2"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceName% -model %ModelName% -mo %ModuleName% -vi %ViewName% -execute -get:csv_mc

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
