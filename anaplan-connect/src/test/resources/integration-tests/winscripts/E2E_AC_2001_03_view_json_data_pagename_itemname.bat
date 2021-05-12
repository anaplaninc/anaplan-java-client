@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc

call config.bat
set AnaplanUser=%UserCredentials%
set FilePath="E2E_AC_2001_Test/E2E_AC_2001_03_view_json_data_pagename_itemname.json"
set ModuleName="view-data-test"
set ViewName="default"
set Pages="view-data-test-dimension:a\\,b"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId% -mo %ModuleName% -view %ViewName% -pages %Pages% -execute -get:json %FilePath%

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
