@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc
call config.bat
set AnaplanUser=%UserCredentials%
set ImportName="coresrv-2776-list from coresrv-2776-list.txt"
set ExportName=""
set FileName="coresrv-2776-list.txt"
set FilePath="..\scripts\data\coresrv-2776-list.txt"
set ChunkSize=1
set ErrorDump="dump\basic-auth-coresrv-2776-list-txt from coresrv-2776-list.txt"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId% -chunksize %ChunkSize% -file %FileName% -put %FilePath% -import %ImportName% -execute -output %ErrorDump%

rem *** End of settings - Do not edit below this line ***

set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
