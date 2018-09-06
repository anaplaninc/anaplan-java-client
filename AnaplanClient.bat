@echo off
rem Assumes this Windows batch script resides in the same directory as
rem anaplan-connect-@rel@.jar, and lib directory containing any additional
rem libraries to be added to the class path.
setlocal enableextensions enabledelayedexpansion || exit /b 1

rem Behavior:
rem 1. If the Java binary path is hardcoded, then use it, ignore everything else (JAVA_HOME, etc.)
rem 2. If Java binary is not hardcoded, then determine JAVA_HOME from environment variable if set,
rem    or fetch it from the Windows Registry (HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Runtime Environment%CurVer%,
rem    or HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Runtime Environment%CurVer%)
rem 3. Build the Java binary path using the JAVA_HOME value, validate that 'JAVA_HOME\bin\java -fullversion' is the valid Java supported, which is Java > 6, so Java7 and Java8.

rem Provide the absolute path to Java8 installation (optional)
rem NOTE: If the customer requires Java_Home to be set to < 1.8, then override this
rem section with the directory for Java 8)
set JAVA=

::::::::::::::::::::::::: Do not edit below this line :::::::::::::::::::::::::

set args=%*

rem Find Java from JAVA_HOME defined or from Windows registry
if [!JAVA!] equ [] (
    echo Absolute path to JAVA 8 executable not provided. Will attempt to figure out path from JAVA_HOME or Windows registry...

    rem Find Java from JAVA_HOME
    if "%JAVA_HOME%" equ "" (
        echo JAVA_HOME is not defined, will attempt to fetch value from registry...
        rem Find Java version
        set CurVer=0
        call :FIND_JAVA_VERSION
        if "!CurVer!" equ "0" call :CANT_FIND_JAVA_VERSION_ERROR
        rem Use the Current registered version of Java to fetch the JAVA_HOME
        set JAVA_HOME="0"
        call :FIND_JAVA_HOME
        echo JAVA_HOME is !JAVA_HOME!
        if "!JAVA_HOME!" equ "0" call :CANT_FIND_JAVA_HOME_ERROR
    )
    set JAVA=!JAVA_HOME!\bin\java.exe
    if not exist !JAVA! call :CANT_FIND_JAVA_ERROR
    echo Found Java executable from JAVA_HOME %JAVA_HOME%, at !JAVA!...
) else (
    echo Java path defined: %JAVA%
)

rem Validate the Java version hardcoded or acquired from JAVA_HOME
if exist "%JAVA%" (
    call :VALIDATE_JAVA_VERSION
) else (
    call :CANT_FIND_JAVA_ERROR
)

rem Finally run Anaplan Connect
call :RUN_CONNECT

goto :eof


:CANT_FIND_JAVA_ERROR
echo ERROR: Java binary at path does not exist (!JAVA!)
exit

:CANT_FIND_JAVA_VERSION_ERROR
echo ERROR: Cannot determine Java version.
exit

:CANT_FIND_JAVA_HOME_ERROR
echo ERROR: Cannot find JAVA_HOME; please set the system variable JAVA_HOME to the location of your Java installation.
exit

:OLD_JAVA_ERROR
echo ERROR: The Java version you are using is not supported by Anaplan Connect. Please upgrade to Java7 or Java8.
exit

:BAD_JAVA_ERROR
echo ERROR: The Java version with major.minor (%1.%2) Java is not supported by Anaplan Connect. Only Java 1.7 and above.
exit

:CANT_FIND_ANAPLAN_CONNECT
echo ERROR: Cannot locate anaplan-connect-@rel@.jar
exit

:VALIDATE_JAVA_VERSION
rem Test for Java version (only Java 8 supported), not Java 6 or 7
set bad_java_version=1.7
for /f "tokens=1,2 delims=." %%A in ("%bad_java_version%") do (
    set bad_version_major=%%A
    set bad_version_minor=%%B
)
set current_java_version=0
for /f tokens^=2-5^ delims^=.-_^" %%j in ('"%JAVA%" -fullversion 2^>^&1') do set "current_java_version=%%j.%%k"
for /f "tokens=1,2 delims=." %%A in ("%current_java_version%") do (
    set current_version_major=%%A
    set current_version_minor=%%B
)
echo Current Java version = %current_version_major%.%current_version_minor%
if !current_version_major! equ !bad_version_major! (
    if !current_version_minor! leq !bad_version_minor! (
        call :OLD_JAVA_ERROR
    ) else (
        echo Current Java version is valid for Anaplan Connect.
    )
) else (
    call :BAD_JAVA_ERROR !current_version_major! !current_version_minor!
)
goto :eof

rem Check registry for JDK
:FIND_JAVA_VERSION
echo Checking Windows Registry for Java Version...
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Runtime Environment" /v CurrentVersion 2^>nul') do set CurVer=%%b
if "!CurVer!" equ "" for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Runtime Environment" /v CurrentVersion 2^>nul') do set CurVer=%%b
if "!CurVer!" equ "" for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Development Kit" /v CurrentVersion 2^>nul') do set CurVer=%%b
if "!CurVer!" equ "" for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Development Kit" /v CurrentVersion 2^>nul') do set CurVer=%%b
goto :eof

rem Check registry for JRE
:FIND_JAVA_HOME
echo Checking Windows Registry for JAVA_HOME...
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Runtime Environment\%CurVer%" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Runtime Environment\%CurVer%" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\Wow6432Node\JavaSoft\Java Development Kit\%CurVer%" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
for /f "skip=2 tokens=2*" %%a in ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Development Kit\%CurVer%" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
goto :eof

:RUN_CONNECT
echo Running Anaplan Connect...
rem Java version is newer than 1.7 so we can continue running
rem Set up the classpath
set HERE=%~dp0
set LIB=%HERE%lib
rem Pick up the most recent Anaplan Connect Jar
set CP=
for /f %%l in ('dir /b /od "%HERE%\anaplan-connect-*.jar"') do set CP=%HERE%%%l
if not exist "%CP%" call :CANT_FIND_ANAPLAN_CONNECT
echo Using Class-Path: %CP%
rem Append anything in lib
if exist "%LIB%" (
    for /f %%l in ('dir /b "%LIB%"') do set CP=!CP!;%LIB%\%%l
) else (
    echo WARNING: cannot find lib directory at %LIB%
)
rem Start the Java virtual machine
"!JAVA!" %JAVA_OPTS% -classpath "%CP%" com.anaplan.client.Program !args!
goto :eof
