#!/bin/bash

usage() {
  echo "Usage: ${0##*/} [appName] [start|stop] [prod|test|dev]"
  exit 1
}

# ---------------------------------------------------------------------------
# Get command line arguments
# ---------------------------------------------------------------------------
APP_NAME=$1
[ "${APP_NAME}" = "" ] && echo "Must provide appName" && usage
COMMAND=$2
RUN_ENV=$3

# ---------------------------------------------------------------------------
# Must have the command
# ---------------------------------------------------------------------------
[ "$COMMAND" = "" ] && usage

# ---------------------------------------------------------------------------
# Is this a valid folder? Must have parent='runtime' or APP_NAME
# ---------------------------------------------------------------------------
echo "Validating run folder"
parent=$(basename `pwd`)
[ "$parent" != "runtime" -a "$parent" != "current" ] && echo "The current folder is not a development or deployment folder: parent=$parent" && usage
echo "-> parentFolder is ${parent}"

# ---------------------------------------------------------------------------
# Determine the correct runtime environment
# ---------------------------------------------------------------------------
echo "Validating environment"
if [ "$RUN_ENV" = "" ] ; then
  echo "Environment not set. Configuring based on folder."
  RUN_ENV="dev" && [ $parent != 'runtime' ] && RUN_ENV="prod"
fi
echo "-> RUN_ENV is ${RUN_ENV}"

# ---------------------------------------------------------------------------
# Set the runtime folder based on environment
# Note that the runtime folder in development is dependent on your build
# system.
# For maven this is ../../../target/classes or ../../target/${APP_NAME}.jar
# For Gradle this is ../../../build/libs/${APP_NAME}.jar
# ---------------------------------------------------------------------------
echo "Determining build system..."
DEV_CLASSPATH="../../../target/classes"
[ -f "../../../build/libs/${APP_NAME}.jar" ] && DEV_CLASSPATH="../../../build/libs/${APP_NAME}.jar"
[ -f "../../../target/${APP_NAME}.jar" ] && DEV_CLASSPATH="../../../target/${APP_NAME}.jar"

echo "-> DEV_CLASSPATH is ${DEV_CLASSPATH}"

# ---------------------------------------------------------------------------
# Configure the RUN_FOLDER and full classpath
# ---------------------------------------------------------------------------
echo "Configuring RUN_FOLDER and base CLASSPATH"
RUN_FOLDER="."
case "$RUN_ENV" in
prod)  echo "Environment: Production"  && CLASSPATH="${RUN_FOLDER}/${APP_NAME}.jar" ;;
dev)   echo "Environment: Development" && CLASSPATH="${DEV_CLASSPATH}"              ;;
*)     echo "Environment: Invalid"     && usage                                     ;;
esac
echo "-> RUN_FOLDER=[$RUN_FOLDER]"
echo "-> CLASSPATH =[$CLASSPATH]"

# ---------------------------------------------------------------------------
# Set the application properties file
# ---------------------------------------------------------------------------
APP_FILE="${RUN_FOLDER}/app.properties"
echo "Extracting ejr properties from ${APP_FILE}"

# ---------------------------------------------------------------------------
# If not found, not possible to run!
# ---------------------------------------------------------------------------
[ ! -f $APP_FILE ] && echo "No $APP_FILE found. Aborting." && usage

# ---------------------------------------------------------------------------
# Find out how much memory to use or default to 128m
# ---------------------------------------------------------------------------
memory=$(grep ejr.memory $APP_FILE | cut -d= -f2 | sed 's/ //g')                && [ "$memory" = "" ]         && echo "ejr.memory        not configured in app.properties. Setting to 128m"          && memory="128m"
minThreads=$(grep ejr.minThreads $APP_FILE | cut -d= -f2 | sed 's/ //g')        && [ "$minThreads" = "" ]     && echo "ejr.minThreads    not configured in app.properties. Setting to 5"             && minThreads="5"
maxThreads=$(grep ejr.maxThreads $APP_FILE | cut -d= -f2 | sed 's/ //g')        && [ "$maxThreads" = "" ]     && echo "ejr.maxThreads    not configured in app.properties. Setting to 50"            && maxThreads="50"
respondOn=$(grep ejr.respondOn $APP_FILE | cut -d= -f2 | sed 's/ //g')          && [ "$respondOn" = "" ]      && echo "ejr.respondOn     not configured in app.properties. Setting to 0.0.0.0:8080"  && respondOn="0.0.0.0:8080"
runtimeFolder=$(grep ejr.runtimeFolder $APP_FILE | cut -d= -f2 | sed 's/ //g')  && [ "$runtimeFolder" = "" ]  && echo "ejr.runtimeFolder not configured in app.properties. Setting to ."             && runtimeFolder="."
echo "-> memory ${memory}"
echo "-> minThreads ${minThreads}"
echo "-> maxThreads ${maxThreads}"
echo "-> respondOn ${respondOn}"
echo "-> runtimeFolder ${runtimeFolder}"

# ---------------------------------------------------------------------------
# Build the class path based on the environment
# ---------------------------------------------------------------------------
CLASSPATH=$DEV_CLASSPATH
if [ -d "${RUN_FOLDER}/lib" ]; then
  for jar in ${RUN_FOLDER}/lib/*.jar ; do
    CLASSPATH="$CLASSPATH:$jar"
  done
elif [ -d "${RUN_FOLDER}/www/WEB-INF/lib" ]; then
   for jar in ${RUN_FOLDER}/www/WEB-INF/lib/*.jar ; do
     CLASSPATH="$CLASSPATH:$jar"
   done
else
  APP_JARS="${RUN_FOLDER}/app.jars"
  [ ! -f "$APP_JARS" ] && echo "Missing app.jars file. Unable to build classpath" && usage
  while read line; do
    CLASSPATH="$CLASSPATH:$line"
  done < <(egrep -v '^#|^$' "${RUN_FOLDER}/app.jars")
fi
echo "-> Derived CLASSPATH=[$CLASSPATH]"

# ---------------------------------------------------------------------------
# Set the main class to run
# ---------------------------------------------------------------------------
MAIN_CLASS=com.alltamasystems.ejr.EJettyMain

# ---------------------------------------------------------------------------
# Set the command line
# ---------------------------------------------------------------------------
D_OPTS="-DrespondOn=${respondOn} -Denv=${RUN_ENV} -DminThreads=${minThreads} -DmaxThreads=${maxThreads} -DruntimeFolder=${runtimeFolder}"
CMD="java -Xmx$memory ${D_OPTS} -cp $CLASSPATH $MAIN_CLASS"

# ---------------------------------------------------------------------------
# Set the application PID (Only relevant in non-dev environments)
# ---------------------------------------------------------------------------
APP_PID="${runtimeFolder}/pid.file"

running() {
  echo "Checking for running..."
  local PID=$(cat "$1" 2>/dev/null) || return 1
  if [ "$PID" != "" ]; then
    echo "Located PID $PID"
    kill -0 "$PID" 2>/dev/null
  else
    echo "Not running"
    return 1
  fi
}

start() {
  echo "Starting: ${APP_NAME}"
  if [ $RUN_ENV = "dev" ]; then
    echo "Running in foreground. ^c to stop"
    echo -e "java -Xmx$memory ${D_OPTS} -cp $(echo "$CLASSPATH" | sed "s/:/\\`echo -e '\n\r    '`/g")\n    $MAIN_CLASS"
    $CMD
  else
    echo "Running PID is ${APP_PID}"
    if [ -f "$APP_PID" ]; then
      if running $APP_PID ; then
        echo "Already running..."
        exit 1
      else
        rm -rf $APP_PID
      fi
    fi
    echo $CMD
	$CMD &
	echo $! > $APP_PID
	echo "Started."
  fi
}

stop() {
  echo "Stopping: ${PROJECT}"
  PID=$(cat "$APP_PID" 2>/dev/null)
  kill "$PID" 2>/dev/null
  TIMEOUT=30
  while running $APP_PID; do
    if (( TIMEOUT-- == 0 )); then
      kill -KILL "$PID" 2>/dev/null
    fi
    sleep 1
  done
  sleep 1
  echo "rm -f $APP_PID"
  rm -f "$APP_PID"
  echo "Stopped."
}

case "$COMMAND" in
  start)   start ;;
  stop)    stop ;;
  restart) echo "Restarting: ${PROJECT}" && stop && start && echo "Restarted" ;;
  *)       usage ;;
esac

exit 0

