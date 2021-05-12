@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc

call config.bat
set AnaplanUser=%UserCredentials%

set ImportName="INTEGRA-2682-テスト GBK from INTEGRA-2682.csv"
set ExportName=""
set FileName="INTEGRA-2682.csv"
set FilePath="data/INTEGRA-2682.csv"
set ChunkSize=1
set OutputName=""
set ErrorDump="dump/INTEGRA-2682-テスト from INTEGRA-2682.txt"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId%  -file %FileName% -put %FilePath% -chunksize %ChunkSize% -import %ImportName% -execute -output %ErrorDump%


rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause

