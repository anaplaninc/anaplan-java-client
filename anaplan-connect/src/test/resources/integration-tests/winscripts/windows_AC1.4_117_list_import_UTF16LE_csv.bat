@echo off
rem This example loads a source text file and runs an Anaplan import into a module.
rem For details of how to configure this script see doc\Anaplan Connect User Guide.doc
call config.bat
set AnaplanUser=%UserCredentials%
set Filename="TabSepCSVUTF16LE.csv"
set FilePath="data\TabSepCSVUTF16LE.csv"
set ImportName="ListUTF16LE from TabSepCSVUTF16LE.csv"
set DumpFile="dump\windows-basic-auth-ListUTF16LE from TabSepCSVUTF16LE.csv"

set Operation=-debug -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId% -chunksize 1 -file %Filename% -put %FilePath%  -import %ImportName% -execute -output %DumpFile%

rem *** End of settings - Do not edit below this line ***

setlocal enableextensions enabledelayedexpansion || exit /b 1
set Credentials=-user %AnaplanUser%
set Command=AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
