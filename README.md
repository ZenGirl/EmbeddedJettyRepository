# Embedded Jetty Runner

This tool allows running a single JAR file as an Jetty instance.
You have full control over the JVM with such things as memory, min/max threads etc.

The system allows you to avoid jar-hell by specifying exactly what JARs you want to include.

The use of an app.properties file allows you to "attach" various classes, war files, public folders and what not to various contexts.

The system works by using a Main-Class attribute set to com.alltamasystems.ejr.EJettyRunner.
This class determines various command line options, reads the app.properties and starts each of the attached resources to the various contexts.
The use of an app.jars file describes the required CLASSPATH for execution.

Examples are provided for:

* Plain Handlers (You can use these to implement filters as well)
* Plain Servlets
* Jersey Servlets
* WAR files
* Exploded WAR folders
* Public Resource folders

It is quite opinionated regarding logging.
It works out the runtime environment (or defined on the cmd line) and uses Slf4j and Log4j to provide precisely formatted output to either the console or a DailyRollingFileAppender.
(Some work may be needed to extend this functionality)

The bash run shell ends up building a single java command execution.

An example:

    java -Xmx=512m -DrespondOn=0.0.0.0:8080 -Denv=prod -DminThreads=5 -DmaxThreads=50 com.yourcompany.project-1.0.jar com.alltamasystems.ejr.EJettyRunner

An app.properties file example:

    # This is the MD5 of "EJettyRocks"
    ejr.secret           = bf7d0eaaec64b80bcd09d6f132ecb567
    ejr.static           = static_files
    ejr.webapp           = www
    ejr.handlers         = com.alltamasystems.ejr.examples.Handler1:/handler1,\
                           com.alltamasystems.ejr.examples.Handler2:/handler2
    ejr.plain.servlets   = com.alltamasystems.ejr.examples.Servlet1:/servlet1,\
                           com.alltamasystems.ejr.examples.Servlet2:/servlet2
    ejr.jersey.servlets  = com.alltamasystems.ejr.examples.Jersey1:/jersey1,\
                           com.alltamasystems.ejr.examples.Jersey2:/jersey2
    ejr.sinatra          = sinatra.app
    ejr.rails            = rails.app

You can, of course, add application specific code to the app.properties file.

A special context named '/ejr' is provided for you to get access to various runtime statistics and control the system.
Here are some examples:

    http://localhost:8080/ejr/stop
    http://localhost:8080/ejr/status
    http://localhost:8080/ejr/routes
    http://localhost:8080/ejr/applog

All of these requests must provide a 'secret' which is an MD5 of the secret specified in the app.properties file.
This is provide by adding a query string: ?secret=bf7d0eaaec64b80bcd09d6f132ecb567

Part of the advantage of using java on the cmd line is that you can easily see what's listening by doing this:

    ps ax | egrep 'java ' | grep -v java | cut -c1-200

Which will easily show what's running and what it's listening on.

# Warning

This is pre-alpha.
I wrote it using maven but would rather have used gradle.
It's not completely functional yet and barely commented as I wrote in one giant session.
I'll be adding stuff gradually over the next few days to get it to a completely usable system.
I'll also be adding sample applications.

I also have to add JRuby-Rack capability so it can run rails/sinatra apps.
Stayed tuned.

# Usage

Just clone it.

To show the tests work, simply do:

    mvn clean test

In reality, you would normally do something like this:

    cd /some/path
    bash run.sh start

I have now added an:

* example app.properties which sets some runtime properties.
* example app.jars file

The app.jars references your .m2 folder. This is sub-optimal.
In reality, and will be shown in the samples, the entire settings are self-generated from the runtime classpath.
To test it, cd to the src/test/runtime folder and do this:

    cd src/test/runtime
    bash com.alltamasystems.ejr start

Since it is running in dev mode, you get console debug.
If you choose prod mode you get logs files instead.



