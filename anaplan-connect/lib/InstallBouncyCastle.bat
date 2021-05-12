@echo off
rem This script install Bouncy Castle jar in the jdk folder
rem It first determines the JAVA_HOME and appends /jre/lib/ext where the bouncy castle gets installed
rem In case of Success or Failure it will print appropriate message on console

echo Installing Bouncy Castle jar......
set java_path=%JAVA_HOME%
echo %java_path%
set jar_path=%java_path%\jre\lib\ext
echo %jar_path%
xcopy %0\..\bcprov-jdk15on-164.jar "%jar_path%" /s
if %errorlevel%==0 (
   echo Installed Bouncy Castle jar Successfully!!!
) else (
   echo There is an error installing Bouncy Castle jar
)
