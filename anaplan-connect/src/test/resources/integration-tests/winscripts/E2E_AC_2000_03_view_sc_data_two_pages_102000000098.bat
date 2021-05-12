@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc

call config.bat
set AnaplanUser=%UserCredentials%
set FilePath=""E2E_AC_2000_Test/E2E_AC_2000_03_view_sc_data_two_pages_102000000098.csv""
set ModuleName="complex-module-2pages"
set ViewId="102000000098"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceName% -model %ModelName% -module %ModuleName% -view %ViewId% -execute -get:csv_sc %FilePath%

#____________________________ Do not edit below this line ______________________________

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
