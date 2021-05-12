@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc

call config.bat
set AnaplanUser=%UserCredentials%

set FileName="upload2636.csv"
set ImportName="Integra_2636_JDBC_Tabs from upload2636.csv"
set #FilePath="~/src/anaplan-connect/2636/upload2636.csv"
set FilePath=""
set ChunkSize=20
set OutputName=""
set ErrorDump="dump/2636.txt"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId%  -file %FileName% -jdbcproperties ac-tests-jdbc.properties -chunksize %ChunkSize% -import %ImportName% -execute -output %ErrorDump%

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause

