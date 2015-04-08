@echo off
rem Assumes this Windows batch script resides in the same directory as
rem anaplan-connect-@rel@.jar, and lib directory containing any additional
rem libraries to be added to the class path.
setlocal enableextensions enabledelayedexpansion || exit /b 1
rem Pick up first java in the path, unless JAVA_HOME is set in environment
if exist "%JAVA_HOME%" goto HAS_JAVA_HOME
set JAVA=
for %%d in (java.exe) do (set JAVA=%%~$PATH:d)
if exist "%JAVA%" goto HAS_JAVA

rem Check registry for JRE
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Runtime Environment" /v CurrentVersion 2^>nul') do set CurVer=%%b
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Runtime Environment\%CurVer%" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
if exist "%JAVA_HOME%" goto HAS_JAVA_HOME
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Runtime Environment" /v CurrentVersion 2^>nul') do set CurVer=%%b
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Runtime Environment\%CurVer%" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
if exist "%JAVA_HOME%" goto HAS_JAVA_HOME

rem Check registry for JDK
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Development Kit" /v CurrentVersion 2^>nul') do set CurVer=%%b
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Development Kit\%CurVer%" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
if exist "%JAVA_HOME%" goto HAS_JAVA_HOME
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Development Kit" /v CurrentVersion 2^>nul') do set CurVer=%%b
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Development Kit\%CurVer%" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
if exist "%JAVA_HOME%" goto HAS_JAVA_HOME

echo Cannot locate a Java runtime; if Java is installed, please set the system variable JAVA_HOME to the location of your Java installation.
exit /b 1

:HAS_JAVA_HOME
set JAVA=%JAVA_HOME%\bin\java.exe

:HAS_JAVA
rem Set up the classpath
set HERE=%~dp0
set LIB=%HERE%lib
rem Pick up the most recent Anaplan Connect Jar
set CP=
for /f %%l in ('dir /b /od "%HERE%\anaplan-connect-*-*-*-*.jar"') do set CP=%HERE%%%l
if exist "%CP%" goto FOUND
echo Cannot locate anaplan-connect-@rel@.jar
exit /b 1
:FOUND
if "%HERE%\anaplan-connect-@rel@.jar" neq "%CP%" echo Using %CP%
rem Append anything in lib
if exist "%LIB%" (
  for /f %%l in ('dir /b "%LIB%"') do set CP=!CP!;%LIB%\%%l
) else (
  echo Warning: cannot find lib directory
)
rem Start the Java virtual machine
"%JAVA%" %JAVA_OPTS% -classpath "%CP%" com.anaplan.client.Program %*
