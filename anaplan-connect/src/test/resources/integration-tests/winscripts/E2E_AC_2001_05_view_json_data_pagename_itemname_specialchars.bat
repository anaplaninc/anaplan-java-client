@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc

call config.bat
set AnaplanUser=%UserCredentials%
set FilePath="E2E_AC_2001_Test/E2E_AC_2001_05_view_json_data_pagename_itemname_specialcharacters.json"
set ModuleId="102000000102"
set ViewId="102000000102"
set Pages="view-data\\:-testlist\\,pages\\::p1\\,p4"


set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId% -module %ModuleId% -view %ViewId% -pages %Pages% -execute -get:json %FilePath%

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
