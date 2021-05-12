@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc
call config.sh
set FileName="ListWithColonSepDoubleQuoDelicsv.csv"
set FilePath="data\ListWithColonSepDoubleQuoDelicsv.csv"
set ImportName="ListWithColonSepDoubleQuoDe from ListWithColonSepDoubleQuoDe"
set DumpFIle="dump\ListWithColonSepDoubleQuoDe from ListWithColonSepDoubleQuoDe"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId% -chunksize 1 -file %FileName% -put %FilePath% -import %ImportName% -execute -output %DumpFIle%

rem *** End of settings - Do not edit below this line ***

rem *** End of settings - Do not edit below this line ***
set Credentials=-k %KeyStorePath% -ka %KeyStoreAlias% -kp %KeyStorePass%
set Command=AnaplanClient.bat %Credentials% %Operation%
%@echo %Command%
cmd /c %Command%
pause
