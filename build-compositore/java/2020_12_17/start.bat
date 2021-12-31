@echo off

if [%1]==[] goto usage
if [%2]==[] goto usage
if [%3]==[] goto usage
if [%4]==[] goto usage
if [%5]==[] goto usage
if [%6]==[] goto usage

set INPUT_FILE=%1
set DB_TYPE=%2
set DB_CONNECTION=%3
set ENTITY=%4
set OUTPUT_FILE=%5
set EXTRA_PARAMETERS=%6

if [%7]==[] (
REM set DEBUG_LEVEL=ewd
set DEBUG_LEVEL=e
) else (
set DEBUG_LEVEL=%7
)

if [%8]==[] (
set DIGIT_SEPARATOR=,.
) else (
set DIGIT_SEPARATOR=%8
)

set CLASSPATH=.\lib\it.saga.library.reportGeneratoreModelli.server.jar
REM set CLASSPATH=%CLASSPATH%;.\lib\it.saga.library.reportGeneratoreModelli.client.jar
set CLASSPATH=%CLASSPATH%;.\lib\it.saga.library.documentiCollegati.server.jar
set CLASSPATH=%CLASSPATH%;.\lib\it.saga.library.documentiCollegati.client.jar
set CLASSPATH=%CLASSPATH%;.\lib\it.saga.library.authentication.client.jar
set CLASSPATH=%CLASSPATH%;.\lib\it.saga.library.messages.client.jar
set CLASSPATH=%CLASSPATH%;.\lib\it.saga.library.logging.client.jar
set CLASSPATH=%CLASSPATH%;.\lib\it.saga.library.security.client.jar
set CLASSPATH=%CLASSPATH%;.\lib\it.saga.library.common.client.jar
set CLASSPATH=%CLASSPATH%;.\lib\aspose_cells.jar
set CLASSPATH=%CLASSPATH%;.\lib\aspose_pdf_internal.jar
set CLASSPATH=%CLASSPATH%;.\lib\aspose_pdf_insane_A.jar
set CLASSPATH=%CLASSPATH%;.\lib\aspose_pdf_insane_B.jar
set CLASSPATH=%CLASSPATH%;.\lib\aspose_pdf_api.jar
set CLASSPATH=%CLASSPATH%;.\lib\gson.jar
set CLASSPATH=%CLASSPATH%;.\lib\ejb-3.6.10.0012.jar
set CLASSPATH=%CLASSPATH%;.\lib\bc-prov.jar
set CLASSPATH=%CLASSPATH%;.\lib\activation-1.1.jar
set CLASSPATH=%CLASSPATH%;.\lib\antlr-runtime-compositore.jar
set CLASSPATH=%CLASSPATH%;.\lib\aspose-words-15.8.0.0000.jar
set CLASSPATH=%CLASSPATH%;.\lib\commons-cli.jar
set CLASSPATH=%CLASSPATH%;.\lib\commons-email.jar
set CLASSPATH=%CLASSPATH%;.\lib\commons-lang-2.6.0.0000.jar
set CLASSPATH=%CLASSPATH%;.\lib\commons-math3.jar
set CLASSPATH=%CLASSPATH%;.\lib\hamcrest-core-1.3.jar
set CLASSPATH=%CLASSPATH%;.\lib\javax.mail-1.5.6.jar
set CLASSPATH=%CLASSPATH%;.\lib\junit.jar
set CLASSPATH=%CLASSPATH%;.\lib\ojdbc6-11.2.0.0003.jar
set CLASSPATH=%CLASSPATH%;.\lib\org.abego.treelayout.core-1.0.1.jar
set CLASSPATH=%CLASSPATH%;.\lib\psqljdbc4-9.3.0.1212.jar
set CLASSPATH=%CLASSPATH%;.\lib\sqljdbc4-4.0.0.0000.jar
set CLASSPATH=%CLASSPATH%;.\lib\rpa-lib-cp.jar
set CLASSPATH=%CLASSPATH%;.\lib\rpa-lib-linux.jar

REM rem Versione generica (da richiamare da un'altro bat)
REM rem "%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" it.saga.library.reportGeneratoreModelli.compositore.compo.RpaMain %1 %2 %3 %4 %5 %6 %7
REM set INPUT_FILE=L:/Test/Java/gare_aeroportopisa_pos/PG/Modelli/LISTA_DITTE_PARTECIPANTI1.rtf
REM REM %1
REM set DB_TYPE=POS
REM REM %2
REM set DB_CONNECTION=DBMS=JDBC;driverClassName=org.postgresql.Driver;url=jdbc:postgresql://WIN-S6B6G8CPN8V:5432/appalti_pos;UID=appalti_pos;PWD=appalti_pos
REM REM %3
REM set ENTITY=GARE:NGARA=G01766
REM REM %4
REM set OUTPUT_FILE=L:/Test/Java/gare_aeroportopisa_pos/PG/Modelli/out/LISTA_DITTE_PARTECIPANTI1_20190513T151551119.rtf
REM REM %5
REM set EXTRA_PARAMETERS=86=48;87=pwb
REM REM 86=48;87=pwb;
REM REM %6
REM set DEBUG_LEVEL=ewd
REM REM %7
REM set DIGIT_SEPARATOR=,.
REM REM %8

REM echo "%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" it.saga.library.reportGeneratoreModelli.compositore.compo.RpaMain -o %INPUT_FILE% -t %DB_TYPE% -u %DB_CONNECTION% -e %ENTITY% -p %EXTRA_PARAMETERS% -n %DIGIT_SEPARATOR% -d %DEBUG_LEVEL% -f %OUTPUT_FILE% -a NOSICRA
"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" it.saga.library.reportGeneratoreModelli.compositore.compo.RpaMain -o %INPUT_FILE% -t %DB_TYPE% -u %DB_CONNECTION% -e %ENTITY% -p %EXTRA_PARAMETERS% -n %DIGIT_SEPARATOR% -d %DEBUG_LEVEL% -f %OUTPUT_FILE% -a NOSICRA

exit %ERRORLEVEL%

: usage
echo.
echo.
echo La sintassi del comando e':
echo start.bat ^<modello in input^> ^<dbms^> ^<connectionstring^> ^<chiave^> ^<file output^>  ^<registri^> ^[separatori numerici^] ^[livello debug^]
echo.
echo Valori di default per parametri opzionali:
echo separatori numerici = ,.
echo livello debug       = 
echo.
echo Esempi:
echo start.bat C:/Eldasoft/AreaShared/PG/Modelli/LISTA_DITTE_PARTECIPANTI.rtf POS "DBMS=JDBC;driverClassName=org.postgresql.Driver;url=jdbc:postgresql://localhost:5432/appalti_pos?charSet=UTF-8;UID=elda_pgpl;PWD=lapassword" "GARE:NGARA=G01766" C:/Eldasoft/AreaShared/PG/Modelli/out/LISTA_DITTE_PARTECIPANTI_20190513T151551119.rtf  "86=48;87=pwb"
echo start.bat C:/Eldasoft/AreaShared/PG/Modelli/LISTA_DITTE_PARTECIPANTI.rtf ORA "DBMS=JDBC;driverClassName=oracle.jdbc.driver.OracleDriver;url=jdbc:oracle:thin:@localhost:1521:ORCL;UID=elda_pgpl;PWD=lapassword" "GARE:NGARA=G01766" C:/Eldasoft/AreaShared/PG/Modelli/out/LISTA_DITTE_PARTECIPANTI_20190513T151551119.rtf  "86=48;87=pwb" ,.
echo start.bat C:/Eldasoft/AreaShared/PG/Modelli/LISTA_DITTE_PARTECIPANTI.rtf MSQ "DBMS=JDBC;driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver;url=jdbc:sqlserver://localhost:1433;charset=iso-8859-1;databaseName=elda_pgpl;UID=elda_pgpl;PWD=lapassword" "GARE:NGARA=G01766" C:/Eldasoft/AreaShared/PG/Modelli/out/LISTA_DITTE_PARTECIPANTI_20190513T151551119.rtf "86=48;87=pwb" ,. ewd
echo.
echo.

: end
