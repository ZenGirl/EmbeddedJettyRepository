# Embedded Jetty Runner Simple AbstractHandler example.

This is a very simple example of a single handler hanging off the context /handler.

To build it, simply cd to the root folder and do this:

    mvn clean install

This will build a jar and generate a SimpleAPI class in target.
An example output is shown below:

    bandit:com.alltamasystems.ejr.maven.simpleHandler kim$ tree target
    target
    ├── classes
    │   └── com
    │       └── alltamasystems
    │           └── ejr
    │               └── maven
    │                   └── simpleHandler
    │                       └── SimpleAPI.class
    ├── com.alltamasystems.ejr.maven.simpleHandler.jar
    ├── generated-sources
    │   ├── annotations
    │   └── test-annotations
    ├── maven-archiver
    │   └── pom.properties
    ├── surefire
    ├── surefire-reports
    │   ├── TEST-com.alltamasystems.ejr.AppTest.xml
    │   └── com.alltamasystems.ejr.AppTest.txt
    └── test-classes
        └── com
            └── alltamasystems
                └── ejr
                    └── AppTest.class

I didn't bother with tests because it's a sample.
Let's have a look at the src/main/runtime folder generated:

    bandit:com.alltamasystems.ejr.maven.simpleHandler kim$ tree src
    src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── alltamasystems
    │   │           └── ejr
    │   │               └── maven
    │   │                   └── simpleHandler
    │   │                       └── SimpleAPI.java
    │   └── runtime
    │       ├── app.properties
    │       ├── lib
    │       │   ├── activation-1.1.jar
    │       │   ├── asm-3.1.jar
    │       │   ├── com.alltamasystems.ejr-1.0.jar
    │       │   ├── hamcrest-core-1.1.jar
    │       │   ├── jackson-core-asl-1.8.3.jar
    │       │   ├── jackson-jaxrs-1.8.3.jar
    │       │   ├── jackson-mapper-asl-1.8.3.jar
    │       │   ├── jackson-xc-1.8.3.jar
    │       │   ├── jaxb-api-2.2.2.jar
    │       │   ├── jaxb-impl-2.2.3-1.jar
    │       │   ├── jersey-client-1.9.1.jar
    │       │   ├── jersey-core-1.9.1.jar
    │       │   ├── jersey-json-1.9.1.jar
    │       │   ├── jersey-server-1.9.1.jar
    │       │   ├── jettison-1.1.jar
    │       │   ├── jettison-1.3.1.jar
    │       │   ├── jetty-continuation-8.0.4.v20111024.jar
    │       │   ├── jetty-http-8.0.4.v20111024.jar
    │       │   ├── jetty-io-8.0.4.v20111024.jar
    │       │   ├── jetty-security-8.0.4.v20111024.jar
    │       │   ├── jetty-server-8.0.4.v20111024.jar
    │       │   ├── jetty-servlet-8.0.4.v20111024.jar
    │       │   ├── jetty-util-8.0.4.v20111024.jar
    │       │   ├── jetty-webapp-8.0.4.v20111024.jar
    │       │   ├── jetty-xml-8.0.4.v20111024.jar
    │       │   ├── junit-4.10.jar
    │       │   ├── log4j-1.2.16.jar
    │       │   ├── servlet-api-2.5.jar
    │       │   ├── servlet-api-3.0.20100224.jar
    │       │   ├── slf4j-api-1.6.2.jar
    │       │   ├── slf4j-log4j12-1.6.2.jar
    │       │   ├── stax-api-1.0-2.jar
    │       │   └── stax-api-1.0.1.jar
    │       ├── logs
    │       │   └── request.2012_05_24.log
    │       └── run.sh
    └── test
        └── java
            └── com
                └── alltamasystems
                    └── ejr
                        └── AppTest.java

    15 directories, 38 files

Now we see that a pom.xml clause has added a shed-load of JARs to the lib folder.
Under normal circumstances I wouldn't do this but would have a 'global deployments lib' folder to send those to, but this is a sample.

Notice the src/main/runtime folder files app.properties and run.sh.
The contents of the app.properties is as follows:

    # ---------------------------------------------------------------------------
    # This is the MD5 of "EJettyRocks"
    # ---------------------------------------------------------------------------
    ejr.secret            = bf7d0eaaec64b80bcd09d6f132ecb567

    # ---------------------------------------------------------------------------
    # This is just a simple handler.
    # ---------------------------------------------------------------------------
    ejr.handlers          = com.alltamasystems.ejr.maven.simpleHandler.SimpleAPI:/handler

    # ---------------------------------------------------------------------------
    # Some examples of setting runtime properties
    # ---------------------------------------------------------------------------
    ejr.memory            = 128m
    ejr.minThreads        = 1
    ejr.maxThreads        = 10
    ejr.respondOn         = 127.0.0.1:8085
    ejr.runtimeFolder     = .

As we can see, we have a secret (not very secure, but this is a sample).
We also have a handler defined and the context it runs off.
We can also see a set of Jetty specific values.

Let's look at the run.sh:

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

Yowza. It's a bit big. But in a real it would look like this:

    #!/bin/bash
    /some/path/to/your/deployments/folder/bin/run.sh $1 $2

Now let's run it in dev mode:

    cd src/main/runtime
    bash run.sh com.alltamasystems.ejr.maven.simpleHandler start
    Validating run folder
    -> parentFolder is runtime
    Validating environment
    Environment not set. Configuring based on folder.
    -> RUN_ENV is dev
    Determining build system...
    -> DEV_CLASSPATH is ../../../target/com.alltamasystems.ejr.maven.simpleHandler.jar
    Configuring RUN_FOLDER and base CLASSPATH
    Environment: Development
    -> RUN_FOLDER=[.]
    -> CLASSPATH =[../../../target/com.alltamasystems.ejr.maven.simpleHandler.jar]
    Extracting ejr properties from ./app.properties
    -> memory 128m
    -> minThreads 1
    -> maxThreads 10
    -> respondOn 127.0.0.1:8085
    -> runtimeFolder .
    -> Derived CLASSPATH=[../../../target/com.alltamasystems.ejr.maven.simpleHandler.jar:./lib/activation-1.1.jar:./lib/asm-3.1.jar:./lib/com.alltamasystems.ejr-1.0.jar:./lib/hamcrest-core-1.1.jar:./lib/jackson-core-asl-1.8.3.jar:./lib/jackson-jaxrs-1.8.3.jar:./lib/jackson-mapper-asl-1.8.3.jar:./lib/jackson-xc-1.8.3.jar:./lib/jaxb-api-2.2.2.jar:./lib/jaxb-impl-2.2.3-1.jar:./lib/jersey-client-1.9.1.jar:./lib/jersey-core-1.9.1.jar:./lib/jersey-json-1.9.1.jar:./lib/jersey-server-1.9.1.jar:./lib/jettison-1.1.jar:./lib/jettison-1.3.1.jar:./lib/jetty-continuation-8.0.4.v20111024.jar:./lib/jetty-http-8.0.4.v20111024.jar:./lib/jetty-io-8.0.4.v20111024.jar:./lib/jetty-security-8.0.4.v20111024.jar:./lib/jetty-server-8.0.4.v20111024.jar:./lib/jetty-servlet-8.0.4.v20111024.jar:./lib/jetty-util-8.0.4.v20111024.jar:./lib/jetty-webapp-8.0.4.v20111024.jar:./lib/jetty-xml-8.0.4.v20111024.jar:./lib/junit-4.10.jar:./lib/log4j-1.2.16.jar:./lib/servlet-api-2.5.jar:./lib/servlet-api-3.0.20100224.jar:./lib/slf4j-api-1.6.2.jar:./lib/slf4j-log4j12-1.6.2.jar:./lib/stax-api-1.0-2.jar:./lib/stax-api-1.0.1.jar]
    Starting: com.alltamasystems.ejr.maven.simpleHandler
    Running in foreground. ^c to stop
    java -Xmx128m -DrespondOn=127.0.0.1:8085 -Denv=dev -DminThreads=1 -DmaxThreads=10 -DruntimeFolder=. -cp ../../../target/com.alltamasystems.ejr.maven.simpleHandler.jar
        ./lib/activation-1.1.jar
        ./lib/asm-3.1.jar
        ./lib/com.alltamasystems.ejr-1.0.jar
        ./lib/hamcrest-core-1.1.jar
        ./lib/jackson-core-asl-1.8.3.jar
        ./lib/jackson-jaxrs-1.8.3.jar
        ./lib/jackson-mapper-asl-1.8.3.jar
        ./lib/jackson-xc-1.8.3.jar
        ./lib/jaxb-api-2.2.2.jar
        ./lib/jaxb-impl-2.2.3-1.jar
        ./lib/jersey-client-1.9.1.jar
        ./lib/jersey-core-1.9.1.jar
        ./lib/jersey-json-1.9.1.jar
        ./lib/jersey-server-1.9.1.jar
        ./lib/jettison-1.1.jar
        ./lib/jettison-1.3.1.jar
        ./lib/jetty-continuation-8.0.4.v20111024.jar
        ./lib/jetty-http-8.0.4.v20111024.jar
        ./lib/jetty-io-8.0.4.v20111024.jar
        ./lib/jetty-security-8.0.4.v20111024.jar
        ./lib/jetty-server-8.0.4.v20111024.jar
        ./lib/jetty-servlet-8.0.4.v20111024.jar
        ./lib/jetty-util-8.0.4.v20111024.jar
        ./lib/jetty-webapp-8.0.4.v20111024.jar
        ./lib/jetty-xml-8.0.4.v20111024.jar
        ./lib/junit-4.10.jar
        ./lib/log4j-1.2.16.jar
        ./lib/servlet-api-2.5.jar
        ./lib/servlet-api-3.0.20100224.jar
        ./lib/slf4j-api-1.6.2.jar
        ./lib/slf4j-log4j12-1.6.2.jar
        ./lib/stax-api-1.0-2.jar
        ./lib/stax-api-1.0.1.jar
        com.alltamasystems.ejr.EJettyMain
    EJettyLogging: Configuring for Development
    EJettyLogging: Configuring rootLogger
    EJettyLogging: Configuring JETTY Console appender
    EJettyLogging: Configuring SITE  Console appender
    EJettyLogging: Configuring com.alltamasystems logger
    EJettyLogging: Configuring org.eclipse logger
    2012-05-24 01:47:18,544 SITE  WARN  [main                ] c.a.e.EJettyMain - EJettyMain: Starting main application
    2012-05-24 01:47:18,550 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: runtimeFolder provided [.]
    2012-05-24 01:47:18,550 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Loading configuration
    2012-05-24 01:47:18,551 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Starting Jetty
    2012-05-24 01:47:18,564 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Setting gracefulShutdown to 2 seconds
    2012-05-24 01:47:18,565 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Configuring thread pool for min 1 and max 10
    2012-05-24 01:47:18,569 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Configuring application to respond on 127.0.0.1 port 8085
    2012-05-24 01:47:18,581 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Configuring request logging to ./logs/request.yyyy_mm_dd.log retention 7 days
    2012-05-24 01:47:18,584 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Adding EJetty handler
    2012-05-24 01:47:18,590 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: EJettyController context /ejr added
    2012-05-24 01:47:18,590 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Attempting to load delivery points
    2012-05-24 01:47:18,591 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: No static files to be delivered
    2012-05-24 01:47:18,591 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Configuring webapp services for /Users/kim/Documents/Projects/EmbeddedJettyRunner/samples/src/maven/com.alltamasystems.ejr.maven.simpleHandler/src/main/runtime
    2012-05-24 01:47:18,612 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Webapp added with context /www
    2012-05-24 01:47:18,612 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: No warfile to be delivered
    2012-05-24 01:47:18,612 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: HANDLER Located "com.alltamasystems.ejr.maven.simpleHandler.SimpleAPI"
    2012-05-24 01:47:18,614 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: HANDLER com.alltamasystems.ejr.maven.simpleHandler.SimpleAPI added with context /handler
    2012-05-24 01:47:18,615 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Plain Handlers configured
    2012-05-24 01:47:18,615 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: No Plain Servlets to be configured
    2012-05-24 01:47:18,615 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: No Jersey Servlets to be configured

As can be seen, it's running in dev mode, so you get output to the screen and can see what was configured.

Now let's make a call to it from another window:

    bandit:~ kim$ curl -L -v http://localhost:8085/handler
    * About to connect() to localhost port 8085 (#0)
    *   Trying ::1... Connection refused
    *   Trying 127.0.0.1... connected
    * Connected to localhost (127.0.0.1) port 8085 (#0)
    > GET /handler HTTP/1.1
    > User-Agent: curl/7.21.4 (universal-apple-darwin11.0) libcurl/7.21.4 OpenSSL/0.9.8r zlib/1.2.5
    > Host: localhost:8085
    > Accept: */*
    >
    < HTTP/1.1 302 Found
    < Location: http://localhost:8085/handler/
    < Content-Length: 0
    < Server: Jetty(8.0.4.v20111024)
    <
    * Connection #0 to host localhost left intact
    * Issue another request to this URL: 'http://localhost:8085/handler/'
    * Re-using existing connection! (#0) with host localhost
    * Connected to localhost (127.0.0.1) port 8085 (#0)
    > GET /handler/ HTTP/1.1
    > User-Agent: curl/7.21.4 (universal-apple-darwin11.0) libcurl/7.21.4 OpenSSL/0.9.8r zlib/1.2.5
    > Host: localhost:8085
    > Accept: */*
    >
    < HTTP/1.1 200 OK
    < Content-Type: application/json;charset=ISO-8859-1
    < Content-Length: 329
    < Server: Jetty(8.0.4.v20111024)
    <
    {
      "status": false,
      "messages": ["Worked"],
      "value": [
        {
          "String1": "Hello",
          "String2": "World",
          "Boolean": false
        },
        {
          "String1": "Hello",
          "String2": "World",
          "Boolean": false
        },
        {
          "String1": "Hello",
          "String2": "World",
          "Boolean": false
        }
      ]
    }
    * Connection #0 to host localhost left intact
    * Closing connection #0

Notice I used the -L option to follow redirects.
Since we're running in dev mode, we can only see request logs:

    127.0.0.1 -  -  [24/May/2012:01:47:31 +0000] "GET /handler HTTP/1.1" 200 0 "-" "curl/7.21.4 (universal-apple-darwin11.0) libcurl/7.21.4 OpenSSL/0.9.8r zlib/1.2.5"
    127.0.0.1 -  -  [24/May/2012:01:47:31 +0000] "GET /handler/ HTTP/1.1" 200 0 "-" "curl/7.21.4 (universal-apple-darwin11.0) libcurl/7.21.4 OpenSSL/0.9.8r zlib/1.2.5"

And in the on-screen log:

    2012-05-24 01:47:31,654 SITE  INFO  [qtp1246657009-17    ] c.a.e.m.s.SimpleAPI - Received GET:/

Ok. Let's check the stats:

    bandit:~ kim$ curl -L -v http://localhost:8085/ejr/status?secret=bf7d0eaaec64b80bcd09d6f132ecb567
    * About to connect() to localhost port 8085 (#0)
    *   Trying ::1... Connection refused
    *   Trying 127.0.0.1... connected
    * Connected to localhost (127.0.0.1) port 8085 (#0)
    > GET /ejr/status?secret=bf7d0eaaec64b80bcd09d6f132ecb567 HTTP/1.1
    > User-Agent: curl/7.21.4 (universal-apple-darwin11.0) libcurl/7.21.4 OpenSSL/0.9.8r zlib/1.2.5
    > Host: localhost:8085
    > Accept: */*
    >
    < HTTP/1.1 200 OK
    < Content-Type: text/plain
    < Content-Length: 274
    < Server: Jetty(8.0.4.v20111024)
    <
    JVM Memory Total (Mb): 81
    JVM Memory Free  (Mb): 78
    JVM Memory Used  (Mb): 2
    JVM Memory Max   (Mb): 125
    Heap Committed   (Mb): 81
    Heap Max         (Mb): 125
    Heap Used        (Mb): 2
    Classes Loaded       : 1486
    Total Classes        : 1486
    Total Threads        : 10
    * Connection #0 to host localhost left intact
    * Closing connection #0

Kool. Now let's look at the routes:

    bandit:~ kim$ curl -L -v http://localhost:8085/ejr/routes?secret=bf7d0eaaec64b80bcd09d6f132ecb567
    * About to connect() to localhost port 8085 (#0)
    *   Trying ::1... Connection refused
    *   Trying 127.0.0.1... connected
    * Connected to localhost (127.0.0.1) port 8085 (#0)
    > GET /ejr/routes?secret=bf7d0eaaec64b80bcd09d6f132ecb567 HTTP/1.1
    > User-Agent: curl/7.21.4 (universal-apple-darwin11.0) libcurl/7.21.4 OpenSSL/0.9.8r zlib/1.2.5
    > Host: localhost:8085
    > Accept: */*
    >
    < HTTP/1.1 200 OK
    < Content-Type: text/plain
    < Content-Length: 1134
    < Server: Jetty(8.0.4.v20111024)
    <
    Server State
      Failed:  false
      Running: true
      Started: true
      Attributes:
      Connectors:
        Port: 8085 Name: 127.0.0.1:8085 LocalPort: 8085
      Connectors:
        Handler: org.eclipse.jetty.server.handler.HandlerCollection@13c695a6#STARTED
          Collection: org.eclipse.jetty.server.handler.HandlerCollection@13c695a6#STARTED
          -> RequestLogHandler: org.eclipse.jetty.server.handler.RequestLogHandler@528acf6e#STARTED
          -> ContextHandler: /ejr
            -> ContextPath:  /ejr
            -> ResourceBase: null
            -> State:        STARTED
            -> Available:    true
            -> Running:      true
          -> ContextHandler: /www
            -> ContextPath:  /www
            -> ResourceBase: file:/Users/kim/Documents/Projects/EmbeddedJettyRunner/samples/src/maven/com.alltamasystems.ejr.maven.simpleHandler/src/main/runtime/
            -> State:        STARTED
            -> Available:    true
            -> Running:      true
          -> ContextHandler: /handler
            -> ContextPath:  /handler
            -> ResourceBase: null
            -> State:        STARTED
            -> Available:    true
            -> Running:      true
    * Connection #0 to host localhost left intact
    * Closing connection #0

Let's stop it:

    bandit:~ kim$ curl -L -v http://localhost:8085/ejr/stop?secret=bf7d0eaaec64b80bcd09d6f132ecb567
    * About to connect() to localhost port 8085 (#0)
    *   Trying ::1... Connection refused
    *   Trying 127.0.0.1... connected
    * Connected to localhost (127.0.0.1) port 8085 (#0)
    > GET /ejr/stop?secret=bf7d0eaaec64b80bcd09d6f132ecb567 HTTP/1.1
    > User-Agent: curl/7.21.4 (universal-apple-darwin11.0) libcurl/7.21.4 OpenSSL/0.9.8r zlib/1.2.5
    > Host: localhost:8085
    > Accept: */*
    >
    < HTTP/1.1 202 Accepted
    < Content-Type: text/plain
    < Content-Length: 16
    < Server: Jetty(8.0.4.v20111024)
    <
    Shutting down.
    * Connection #0 to host localhost left intact
    * Closing connection #0

And the log:

    2012-05-24 02:11:34,213 SITE  WARN  [qtp1246657009-42    ] c.a.e.EJettyController - Stopping Jetty
    2012-05-24 02:11:34,214 SITE  WARN  [qtp1246657009-42    ] c.a.e.EJettyController - EJetty request completed
    2012-05-24 02:11:34,216 SITE  INFO  [Thread-33           ] c.a.e.EJettyController - Shutting down Jetty...
    2012-05-24 02:11:36,289 SITE  WARN  [main                ] c.a.e.EJettyRunner - Jetty stopped
    2012-05-24 02:11:36,289 SITE  WARN  [main                ] c.a.e.EJettyMain - EJettyMain: Main application returned 0
    2012-05-24 02:11:36,289 SITE  INFO  [Thread-33           ] c.a.e.EJettyController - Jetty has stopped.

Let's run it in prod mode:

    bandit:runtime kim$ bash run.sh com.alltamasystems.ejr.maven.simpleHandler start prod
    Validating run folder
    -> parentFolder is runtime
    Validating environment
    -> RUN_ENV is prod
    Determining build system...
    -> DEV_CLASSPATH is ../../../target/com.alltamasystems.ejr.maven.simpleHandler.jar
    Configuring RUN_FOLDER and base CLASSPATH
    Environment: Production
    -> RUN_FOLDER=[.]
    -> CLASSPATH =[./com.alltamasystems.ejr.maven.simpleHandler.jar]
    Extracting ejr properties from ./app.properties
    -> memory 128m
    -> minThreads 1
    -> maxThreads 10
    -> respondOn 127.0.0.1:8085
    -> runtimeFolder .
    -> Derived CLASSPATH=[../../../target/com.alltamasystems.ejr.maven.simpleHandler.jar:./lib/activation-1.1.jar:./lib/asm-3.1.jar:./lib/com.alltamasystems.ejr-1.0.jar:./lib/hamcrest-core-1.1.jar:./lib/jackson-core-asl-1.8.3.jar:./lib/jackson-jaxrs-1.8.3.jar:./lib/jackson-mapper-asl-1.8.3.jar:./lib/jackson-xc-1.8.3.jar:./lib/jaxb-api-2.2.2.jar:./lib/jaxb-impl-2.2.3-1.jar:./lib/jersey-client-1.9.1.jar:./lib/jersey-core-1.9.1.jar:./lib/jersey-json-1.9.1.jar:./lib/jersey-server-1.9.1.jar:./lib/jettison-1.1.jar:./lib/jettison-1.3.1.jar:./lib/jetty-continuation-8.0.4.v20111024.jar:./lib/jetty-http-8.0.4.v20111024.jar:./lib/jetty-io-8.0.4.v20111024.jar:./lib/jetty-security-8.0.4.v20111024.jar:./lib/jetty-server-8.0.4.v20111024.jar:./lib/jetty-servlet-8.0.4.v20111024.jar:./lib/jetty-util-8.0.4.v20111024.jar:./lib/jetty-webapp-8.0.4.v20111024.jar:./lib/jetty-xml-8.0.4.v20111024.jar:./lib/junit-4.10.jar:./lib/log4j-1.2.16.jar:./lib/servlet-api-2.5.jar:./lib/servlet-api-3.0.20100224.jar:./lib/slf4j-api-1.6.2.jar:./lib/slf4j-log4j12-1.6.2.jar:./lib/stax-api-1.0-2.jar:./lib/stax-api-1.0.1.jar]
    Starting: com.alltamasystems.ejr.maven.simpleHandler
    Running PID is ./pid.file
    java -Xmx128m -DrespondOn=127.0.0.1:8085 -Denv=prod -DminThreads=1 -DmaxThreads=10 -DruntimeFolder=. -cp ../../../target/com.alltamasystems.ejr.maven.simpleHandler.jar:./lib/activation-1.1.jar:./lib/asm-3.1.jar:./lib/com.alltamasystems.ejr-1.0.jar:./lib/hamcrest-core-1.1.jar:./lib/jackson-core-asl-1.8.3.jar:./lib/jackson-jaxrs-1.8.3.jar:./lib/jackson-mapper-asl-1.8.3.jar:./lib/jackson-xc-1.8.3.jar:./lib/jaxb-api-2.2.2.jar:./lib/jaxb-impl-2.2.3-1.jar:./lib/jersey-client-1.9.1.jar:./lib/jersey-core-1.9.1.jar:./lib/jersey-json-1.9.1.jar:./lib/jersey-server-1.9.1.jar:./lib/jettison-1.1.jar:./lib/jettison-1.3.1.jar:./lib/jetty-continuation-8.0.4.v20111024.jar:./lib/jetty-http-8.0.4.v20111024.jar:./lib/jetty-io-8.0.4.v20111024.jar:./lib/jetty-security-8.0.4.v20111024.jar:./lib/jetty-server-8.0.4.v20111024.jar:./lib/jetty-servlet-8.0.4.v20111024.jar:./lib/jetty-util-8.0.4.v20111024.jar:./lib/jetty-webapp-8.0.4.v20111024.jar:./lib/jetty-xml-8.0.4.v20111024.jar:./lib/junit-4.10.jar:./lib/log4j-1.2.16.jar:./lib/servlet-api-2.5.jar:./lib/servlet-api-3.0.20100224.jar:./lib/slf4j-api-1.6.2.jar:./lib/slf4j-log4j12-1.6.2.jar:./lib/stax-api-1.0-2.jar:./lib/stax-api-1.0.1.jar com.alltamasystems.ejr.EJettyMain
    Started.
    bandit:runtime kim$ EJettyLogging: Configuring for Production
    EJettyLogging: Configuring rootLogger
    EJettyLogging: Configuring JETTY RollingFile appender
    EJettyLogging: Configuring SITE  RollingFile appender
    EJettyLogging: Configuring com.alltamasystems logger INFO
    EJettyLogging: Configuring org.eclipse logger WARN

Notice that we get no logging to the screen
The src/main/runtime/logs folder now has a DailyRollingFileAppender added to it.
Let's look at it:

    2012-05-24 02:12:24,714 SITE  WARN  [main                ] c.a.e.EJettyMain - EJettyMain: Starting main application
    2012-05-24 02:12:24,720 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: runtimeFolder provided [.]
    2012-05-24 02:12:24,720 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Loading configuration
    2012-05-24 02:12:24,721 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Starting Jetty
    2012-05-24 02:12:24,734 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Setting gracefulShutdown to 2 seconds
    2012-05-24 02:12:24,736 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Configuring thread pool for min 1 and max 10
    2012-05-24 02:12:24,740 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Configuring application to respond on 127.0.0.1 port 8085
    2012-05-24 02:12:24,756 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Configuring request logging to ./logs/request.yyyy_mm_dd.log retention 7 days
    2012-05-24 02:12:24,758 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Adding EJetty handler
    2012-05-24 02:12:24,764 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: EJettyController context /ejr added
    2012-05-24 02:12:24,764 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Attempting to load delivery points
    2012-05-24 02:12:24,764 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: No static files to be delivered
    2012-05-24 02:12:24,764 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Configuring webapp services for /Users/kim/Documents/Projects/EmbeddedJettyRunner/samples/src/maven/com.alltamasystems.ejr.maven.simpleHandler/src/main/runtime
    2012-05-24 02:12:24,785 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Webapp added with context /www
    2012-05-24 02:12:24,785 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: No warfile to be delivered
    2012-05-24 02:12:24,786 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: HANDLER Located "com.alltamasystems.ejr.maven.simpleHandler.SimpleAPI"
    2012-05-24 02:12:24,788 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: HANDLER com.alltamasystems.ejr.maven.simpleHandler.SimpleAPI added with context /handler
    2012-05-24 02:12:24,788 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: Plain Handlers configured
    2012-05-24 02:12:24,788 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: No Plain Servlets to be configured
    2012-05-24 02:12:24,788 SITE  WARN  [main                ] c.a.e.EJettyRunner - JETTY: No Jersey Servlets to be configured

Same as what went to the screen, but without extraneous junk.

So endeth the first simple lesson.





























