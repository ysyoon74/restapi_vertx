@echo off
rem ---------------------------------------------------------------------------
rem Start script for the Document Filter By Tika
rem ---------------------------------------------------------------------------

setlocal EnableDelayedExpansion

set JAVA=java

set CONF_DIR=.\conf

set CLASSPATH=.\bin

for /R .\lib_bin %%F in (*.jar) do (
 	set CLASSPATH=!CLASSPATH!;.\lib_bin\%%~nF.jar
)

for /R .\lib %%F in (*.jar) do (
 	set CLASSPATH=!CLASSPATH!;.\lib\%%~nF.jar
)

set JVM_OPTS=-DIN2_DOCUMENT_FILTER

set JVM_OPTS_FILE=jvm.options

for /f "delims= eol=#" %%L in (%JVM_OPTS_FILE%) do (
	set JVM_OPTS=!JVM_OPTS! %%L	
)

%JAVA% -classpath "%CLASSPATH%" %JVM_OPTS% u.cando.restapi.server.EmbeddedRestApiServer -conf=./conf/restapi-conf.json