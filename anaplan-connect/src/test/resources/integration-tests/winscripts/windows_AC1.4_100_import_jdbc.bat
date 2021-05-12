@echo off
call config.bat

set AnaplanUser=%UserCredentials%
set ImportName="Data from Gaga from dataFromGaga.csv"
set FileName="dataFromGaga.csv"



rem Step1 : To create a datasource, run this first as a super support user(because you are rem creating a data source).
rem set Operation= -debug -auth %AuthUrl% -service %ServiceUrl% -workspace %WorkspaceId% -model %ModelId% -loadclass %JdbcDriver% -file %FileName% -jdbcproperties "jdbc_query.properties"

set Operation=-debug -file %FileName% -jdbcproperties %JdbcProperties% -import %ImportName%  -service %ServiceUrl% -auth %AuthUrl% -workspace %WorkspaceId% -model %ModelId%  -loadclass %JdbcDriver% -execute -output 'dump\windows-JDBCErrors.txt'


rem *** End of settings - Do not edit below this line ***
set Command=.\AnaplanClient.bat %Credentials% %Operation%
@echo %Command%
cmd /c %Command%
pause
