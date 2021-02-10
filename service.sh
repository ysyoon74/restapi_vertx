#!/bin/bash

APP_NAME="RestAPI"
DOCKER_NAME="RESTAPI"
MAIN_CLASS="u.cando.restapi.server.EmbeddedRestApiServer"
MAIN_PARAMETERS="-conf=./conf/restapi-conf.json"

if [ "x$IN2_HOME" = "x" ]; then
    IN2_HOME="`dirname "$0"`/."
fi

IN2_CONF="$IN2_HOME/conf"

PID="$IN2_HOME/pid/${DOCKER_NAME}.pid"

# The java classpath (required)
CLASSPATH="$IN2_HOME/bin"

for jar in "$IN2_HOME"/lib_bin/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

for jar in "$IN2_HOME"/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

# Determine the sort of JVM we'll be running on.
java_ver_output=`"${JAVA:-java}" -version 2>&1`
jvmver=`echo "$java_ver_output" | grep '[openjdk|java] version' | awk -F'"' 'NR==1 {print $2}' | cut -d\- -f1`
JVM_VERSION=${jvmver%_*}
JVM_PATCH_VERSION=${jvmver#*_}

if [ "$JVM_VERSION" \< "1.8" ] ; then
    echo "$APP_NAME and later require Java 8 or later."
    exit 1;
fi

if [ "$JVM_VERSION" \< "1.8" ] ; then
    echo "$APP_NAME and later require Java 8 or later."
    exit 1;
fi

jvm=`echo "$java_ver_output" | grep -A 1 '[openjdk|java] version' | awk 'NR==2 {print $1}'`

case "$jvm" in
    OpenJDK)
        JVM_VENDOR=OpenJDK
        # this will be "64-Bit" or "32-Bit"
        JVM_ARCH=`echo "$java_ver_output" | awk 'NR==3 {print $2}'`
        ;;
    "Java(TM)")
        JVM_VENDOR=Oracle
        # this will be "64-Bit" or "32-Bit"
        JVM_ARCH=`echo "$java_ver_output" | awk 'NR==3 {print $3}'`
        ;;
    *)
        # Help fill in other JVM values
        JVM_VENDOR=other
        JVM_ARCH=unknown
        ;;
esac

JVM_OPTS="-D${DOCKER_NAME}"

# Read user-defined JVM options from jvm.options file
JVM_OPTS_FILE=jvm.options

for opt in `grep "^-" $JVM_OPTS_FILE`
do
  JVM_OPTS="$JVM_OPTS $opt"
done


# colors
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
reset='\e[0m'

echoRed() { echo -e "${red}$1${reset}"; }
echoGreen() { echo -e "${green}$1${reset}"; }
echoYellow() { echo -e "${yellow}$1${reset}"; }

# Check whether the application is running.
# The check is pretty simple: open a running pid file and check that the process
# is alive.
isrunning() {
  # Check for running app
  if [ -f "$PID" ]; then
    proc=$(cat $PID);
    if /bin/ps --pid $proc 1>&2 >/dev/null;
    then
      return 0
    fi
  fi
  return 1
}

start() {
  if isrunning; then
    echoYellow "The ${APP_NAME} application is already running"
    return 0
  fi

  nohup java ${JVM_OPTS} -classpath "$CLASSPATH" "$MAIN_CLASS" ${MAIN_PARAMETERS} > /dev/null 2>&1 &

  echo $! > ${PID}

  if isrunning; then
    echoGreen "${APP_NAME} Application started"
    exit 0
  else
    echoRed "The ${APP_NAME} Application has not started - check log"
    exit 3
  fi
}

console() {
  if isrunning; then
    echoYellow "The ${APP_NAME} application is already running"
    return 0
  fi

  java ${JVM_OPTS} -classpath "$CLASSPATH" "$MAIN_CLASS" ${MAIN_PARAMETERS}
}

restart() {
  echo "Restarting ${APP_NAME} Application"
  stop
  start
}

stop() {
  echoYellow "Stopping ${APP_NAME} Application"
  if isrunning; then
    kill `cat $PID`
    rm $PID
  fi
}

status() {
  if isrunning; then
    proc=$(cat $PID);
    echoGreen "${APP_NAME} Application is running: ${proc}"
  else
    echoRed "${APP_NAME} Application is either stopped or inaccessible"
  fi
}

case "$1" in
start)
    start
;;

console)
    console
;;

status)
   status
   exit 0
;;

stop)
    if isrunning; then
	stop
	exit 0
    else
	echoRed "${APP_NAME} Application not running"
	exit 3
    fi
;;

restart)
    stop
    start
;;

*)
    echo "Usage: $0 {status|start|console|stop|restart}"
    exit 1
esac

