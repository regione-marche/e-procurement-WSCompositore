#!/bin/bash

if [ -z "$1" ]; then 
	echo "Inserire modello in input"
	exit -3
fi
if [ -z "$2" ]; then 
	echo "Inserire dbms (ORA, POS o MSQ)"
	exit -3
fi
if [ -z "$3" ]; then 
	echo "Inserire la connectionstring java"
	exit -3
fi
if [ -z "$4" ]; then 
	echo "Inserire la chiave entita nel formato ENTITA.CAMPO1=VALORE1;ENTITA.CAMPO2=VALORE"
	exit -3
fi
if [ -z "$5" ]; then 
	echo "Inserire il file di output"
	exit -3
fi
if [ -z "$6" ]; then 
	echo "Inserire i registri"
	exit -3
fi

INPUT_FILE=$1
DB_TYPE=$2
DB_CONNECTION=$3
ENTITY=$4
OUTPUT_FILE=$5
EXTRA_PARAMETERS=$6

if [ -z "$7" ]; then
	DEBUG_LEVEL=e
else
	DEBUG_LEVEL=$7
fi

if [ -z "$8" ]; then
	DIGIT_SEPARATOR=,.
else
	DIGIT_SEPARATOR=$8
fi

CLASSPATH=./lib/it.saga.library.reportGeneratoreModelli.server.jar
#CLASSPATH=$CLASSPATH:./lib/it.saga.library.reportGeneratoreModelli.client.jar
CLASSPATH=$CLASSPATH:./lib/it.saga.library.documentiCollegati.server.jar
CLASSPATH=$CLASSPATH:./lib/it.saga.library.documentiCollegati.client.jar
CLASSPATH=$CLASSPATH:./lib/it.saga.library.authentication.client.jar
CLASSPATH=$CLASSPATH:./lib/it.saga.library.messages.client.jar
CLASSPATH=$CLASSPATH:./lib/it.saga.library.logging.client.jar
CLASSPATH=$CLASSPATH:./lib/it.saga.library.security.client.jar
CLASSPATH=$CLASSPATH:./lib/it.saga.library.common.client.jar
CLASSPATH=$CLASSPATH:./lib/aspose_cells.jar
CLASSPATH=$CLASSPATH:./lib/aspose_pdf_internal.jar
CLASSPATH=$CLASSPATH:./lib/aspose_pdf_insane_A.jar
CLASSPATH=$CLASSPATH:./lib/aspose_pdf_insane_B.jar
CLASSPATH=$CLASSPATH:./lib/aspose_pdf_api.jar
CLASSPATH=$CLASSPATH:./lib/gson.jar
CLASSPATH=$CLASSPATH:./lib/ejb-3.6.10.0012.jar
CLASSPATH=$CLASSPATH:./lib/bc-prov.jar
CLASSPATH=$CLASSPATH:./lib/activation-1.1.jar
CLASSPATH=$CLASSPATH:./lib/antlr-runtime-compositore.jar
CLASSPATH=$CLASSPATH:./lib/aspose-words-15.8.0.0000.jar
CLASSPATH=$CLASSPATH:./lib/commons-cli.jar
CLASSPATH=$CLASSPATH:./lib/commons-email.jar
CLASSPATH=$CLASSPATH:./lib/commons-lang-2.6.0.0000.jar
CLASSPATH=$CLASSPATH:./lib/commons-math3.jar
CLASSPATH=$CLASSPATH:./lib/hamcrest-core-1.3.jar
CLASSPATH=$CLASSPATH:./lib/javax.mail-1.5.6.jar
CLASSPATH=$CLASSPATH:./lib/junit.jar
CLASSPATH=$CLASSPATH:./lib/ojdbc6-11.2.0.0003.jar
CLASSPATH=$CLASSPATH:./lib/org.abego.treelayout.core-1.0.1.jar
CLASSPATH=$CLASSPATH:./lib/psqljdbc4-9.3.0.1212.jar
CLASSPATH=$CLASSPATH:./lib/sqljdbc4-4.0.0.0000.jar
CLASSPATH=$CLASSPATH:./lib/rpa-lib-cp.jar
CLASSPATH=$CLASSPATH:./lib/rpa-lib-linux.jar

# # Parametri interni
# INPUT_FILE=./test/elenco_pratiche1.rtf
# DB_TYPE=POS
# ENTITY=APE_CONCES:PKID=39903
# DB_CONNECTION="DBMS=JDBC;driverClassName=org.postgresql.Driver;url=jdbc:postgresql://wf81collx:5432/olgiate?charSet=UTF-8;UID=sicraweb;PWD=sicraweb"
# EXTRA_PARAMETERS=87=ape;80=300215
# DEBUG_LEVEL=dwe
# DIGIT_SEPARATOR=,.

#echo -------------------------------------------------------------------------------------------------
#echo $CLASSPATH
#echo -------------------------------------------------------------------------------------------------
$JAVA_HOME/bin/java -classpath $CLASSPATH it.saga.library.reportGeneratoreModelli.compositore.compo.RpaMain -o $INPUT_FILE -t $DB_TYPE -u $DB_CONNECTION -e $ENTITY -p $EXTRA_PARAMETERS -n $DIGIT_SEPARATOR -d $DEBUG_LEVEL -f $OUTPUT_FILE -a NOSICRA

exit $?

