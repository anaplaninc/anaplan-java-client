@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc
call config.bat
set FileName="ListWithColonSepSingleQuoDeli.txt"
set FilePath="data\ListWithColonSepSingleQuoDeli.txt"
set ImportName="ListWithColonSepSingleQuoDe from ListWithColonSepSingleQuo~1"
set DumpFile="dump\ListWithColonSepDoubleQuoDe from ListWithColonSepDoubleQuoDe"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId% -chunksize 1 -file %FileName% -put %FilePath% -import %ImportName% -execute -output %DumpFile%

rem *** End of settings - Do not edit below this line ***

rem *** End of settings - Do not edit below this line ***
set Credentials=-k %KeyStorePath% -ka %KeyStoreAlias% -kp %KeyStorePass%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
