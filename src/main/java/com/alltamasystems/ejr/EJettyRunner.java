package com.alltamasystems.ejr;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 18/05/12
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class EJettyRunner {

  private static Logger log = LoggerFactory.getLogger(EJettyRunner.class);

  private String env;
  private String ipAddress;
  private int port;
  private int minThreads = 5;
  private int maxThreads = 50;
  private String ejettySecret = "bf7d0eaaec64b80bcd09d6f132ecb567";
  private boolean runningInDevelopment = false;
  private String runtimeFolder = ".";

  public EJettyRunner(String env, String ipAddress, int portNumber, int minThreadsNumber, int maxThreadsNumber, String runtimeFolder) {
    this.env = env;
    this.ipAddress = ipAddress;
    this.port = portNumber;
    this.minThreads = minThreadsNumber;
    this.maxThreads = maxThreadsNumber;
    runningInDevelopment = (env.equals("dev"));
    if (runtimeFolder != null && !runtimeFolder.trim().isEmpty()) {
      File rf = new File(runtimeFolder);
      if (!rf.exists()) {
        log.warn("JETTY: runtimeFolder provided [" + runtimeFolder + "] not found");
      } else {
        if (!rf.isDirectory() && !rf.canRead()) {
          log.warn("JETTY: runtimeFolder provided [" + runtimeFolder + "] not readable");
        } else {
          log.warn("JETTY: runtimeFolder provided [" + runtimeFolder + "]");
          this.runtimeFolder = runtimeFolder;
        }
      }
    }
  }

  public int start() throws Exception {
    while (true) {
      log.warn("JETTY: Loading configuration");
      String applicationProperties = "app.properties";
      File f = new File(applicationProperties);
      if (!f.exists()) {
        log.warn("JETTY: No configuration found " + applicationProperties);
        applicationProperties = runtimeFolder + "/app.properties";
        f = new File(applicationProperties);
        if (!f.exists()) {
          log.warn("JETTY: No configuration found " + applicationProperties);
          return(1);
        }
      }
      Properties props = new Properties();
      try {
        props.load(new FileInputStream(applicationProperties));
      } catch (IOException e) {
        log.error("JETTY: Error reading " + applicationProperties + ": " + e);
        break;
      }

      log.warn("JETTY: Starting Jetty");
      HandlerCollection handlers = new HandlerCollection(); // We need a collection of handlers and contexts

      Server server = new Server();
      log.warn("JETTY: Setting gracefulShutdown to 2 seconds");
      server.setGracefulShutdown(2000);
      server.setStopAtShutdown(true);
      server.setHandler(handlers);

      log.warn("JETTY: Configuring thread pool for min " + minThreads + " and max " + maxThreads);
      QueuedThreadPool threadPool = new QueuedThreadPool();
      threadPool.setMinThreads(minThreads);
      threadPool.setMaxThreads(maxThreads);
      server.setThreadPool(threadPool);

      log.warn("JETTY: Configuring application to respond on " + ipAddress + " port " + port);
      SelectChannelConnector connector = new SelectChannelConnector();
      connector.setPort(port);
      connector.setHost(ipAddress);
      server.addConnector(connector);

      String requestLogName = "logs/request.yyyy_mm_dd.log";
      log.warn("JETTY: Configuring request logging to " + requestLogName + " retention 7 days");
      f = new File("logs");
      if (!f.exists()) {
        log.warn("logs folder not found. Creating...");
        f.mkdir();
      }
      RequestLogHandler requestLogHandler = new RequestLogHandler();
      NCSARequestLog requestLog = new NCSARequestLog(requestLogName);
      requestLog.setAppend(true);
      requestLog.setExtended(true);
      requestLog.setLogTimeZone("UTC");
      requestLog.setRetainDays(7);
      requestLogHandler.setRequestLog(requestLog);
      handlers.addHandler(requestLogHandler);

      log.warn("JETTY: Adding EJetty handler");
      EJettyController ejettyHandler = new EJettyController(server, ejettySecret);
      ContextHandler ejettyContext = new ContextHandler();
      ejettyContext.setContextPath("/ejr");
      ejettyContext.setHandler(ejettyHandler);
      ejettyContext.setClassLoader(Thread.currentThread().getContextClassLoader());
      handlers.addHandler(ejettyContext);
      log.warn("JETTY: EJettyController context /ejr added");

      log.warn("JETTY: Attempting to load delivery points");

      // Static files
      String staticFiles = props.getProperty("ejr.static", "");
      if (staticFiles.trim().length() == 0) {
        log.warn("JETTY: No static files to be delivered");
      } else {
        f = new File(runtimeFolder + "/static_files");
        if (!f.exists()) {
          log.error("JETTY: Static folder " + f.getCanonicalPath() + " not found");
        } else {
          log.warn("JETTY: Configuring static file services for " + f.getCanonicalPath());
          ResourceHandler resourceHandler = new ResourceHandler();
          resourceHandler.setDirectoriesListed(true);
          resourceHandler.setResourceBase(f.getCanonicalPath());
          ContextHandler publicContext = new ContextHandler();
          publicContext.setContextPath("/static");
          publicContext.setClassLoader(Thread.currentThread().getContextClassLoader());
          publicContext.setHandler(resourceHandler);
          handlers.addHandler(publicContext);
          log.warn("JETTY: Handler added with context /static");
        }
      }

      // Webapps
      String webapp = runtimeFolder + "/" + props.getProperty("ejr.webapp", "");
      if (webapp.trim().length() == 0) {
        log.warn("JETTY: No webapp to be delivered");
      } else {
        f = new File(webapp);
        if (!f.exists()) {
          log.error("JETTY: Webapp folder " + f.getCanonicalPath() + " not found");
        } else {
          log.warn("JETTY: Configuring webapp services for " + f.getCanonicalPath());
          WebAppContext context = new WebAppContext();
          context.setContextPath("/www");
          context.setResourceBase(f.getCanonicalPath());
          context.setDescriptor(f.getCanonicalPath() + "/WEB-INF/web.xml");
          context.setParentLoaderPriority(true);
          handlers.addHandler(context);
          log.warn("JETTY: Webapp added with context /www");
        }
      }

      // War File
      String warfile = props.getProperty("ejr.warfile", "");
      if (warfile.trim().length() == 0) {
        log.warn("JETTY: No warfile to be delivered");
      } else {
        f = new File(warfile);
        if (!f.exists()) {
          log.error("JETTY: WarFile folder " + f.getCanonicalPath() + " not found");
        } else {
          WebAppContext context = new WebAppContext();
          context.setContextPath("/war");
          context.setWar(warfile);
//          context.setResourceBase(f.getCanonicalPath());
//          context.setDescriptor(f.getCanonicalPath() + "/WEB-INF/web.xml");
//          context.setParentLoaderPriority(true);
          handlers.addHandler(context);
          log.warn("JETTY: Webapp added with context /www");
        }
      }

      // Handlers
      String plainHandlers = props.getProperty("ejr.handlers", "").trim();
      if (plainHandlers.length() == 0) {
        log.warn("JETTY: No Plain Handlers to be configured");
      } else {
        for (String nameAndContext : plainHandlers.split(",")) {
          String parts[] = nameAndContext.trim().split(":");
          if (parts != null && parts.length > 1) {
            String clazzName = parts[0].trim();
            if (clazzName.length() <= 0) {
              log.error("JETTY: Plain Handlers malformed: [" + nameAndContext + "]");
              continue;
            }
            String contextPath = parts[1].trim();
            if (contextPath.length() <= 0) {
              log.error("JETTY: Plain Handlers malformed: [" + nameAndContext + "]");
              continue;
            }
            try {
              log.warn("JETTY: HANDLER Located \"" + clazzName + "\"");
              Class clazz = Class.forName(clazzName);
              Constructor ctor = clazz.getConstructor();
              Handler handler = (Handler) ctor.newInstance(); // Construct the handler

              ContextHandler context = new ContextHandler(); // Get a context
              context.setContextPath(contextPath); // Notice we specify the exact context path here!
              context.setClassLoader(Thread.currentThread().getContextClassLoader());
              context.setHandler(handler); // Add it to the context
              handlers.addHandler(context); // And add the context to the handlers
              log.warn("JETTY: HANDLER " + clazz.getCanonicalName() + " added with context " + contextPath);
            } catch (ClassNotFoundException ex) {
              log.error("JETTY: HANDLER Unable to load class: " + clazzName + " - " + ex);
            }
          } else {
            log.warn("JETTY: No handlers defined");
          }
        }
        log.warn("JETTY: Plain Handlers configured");
      }

      // Plain Servlets
      String plainServlets = props.getProperty("ejr.plain.servlets", "").trim();
      if (plainServlets.length() == 0) {
        log.warn("JETTY: No Plain Servlets to be configured");
      } else {
        for (String nameAndContext : plainServlets.split(",")) {
          String parts[] = nameAndContext.trim().split(":");
          if (parts != null && parts.length > 1) {
            String clazzName = parts[0].trim();
            if (clazzName.length() <= 0) {
              log.error("JETTY: Plain Servlets malformed: [" + nameAndContext + "]");
              continue;
            }
            String contextPath = parts[1].trim();
            if (contextPath.length() <= 0) {
              log.error("JETTY: Plain Servlets malformed: [" + nameAndContext + "]");
              continue;
            }
            try {
              log.warn("JETTY: PLAIN SERVLET Located \"" + clazzName + "\"");
              Class clazz = Class.forName(clazzName);
              Constructor ctor = clazz.getConstructor();
              HttpServlet servlet = (HttpServlet) ctor.newInstance(); // Construct the handler

              // Get a SERVLET context
              ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
              context.setContextPath("/"); // Notice we set ROOT as the context path
              context.setClassLoader(Thread.currentThread().getContextClassLoader());
              context.addServlet(new ServletHolder(servlet), contextPath); // Add it to the context against the context path
              handlers.addHandler(context); // And add the context to the handlers
              log.warn("JETTY: PLAIN SERVLET " + clazz.getCanonicalName() + " added with context " + contextPath);
            } catch (ClassNotFoundException ex) {
              log.error("JETTY: PLAIN SERVLET Unable to load class: " + clazzName + " - " + ex);
            }
          }
        }
        log.warn("JETTY: Plain Servlets configured");
      }

      // Jersey Servlets
      String jerseyServlets = props.getProperty("ejr.jersey.servlets", "").trim();
      if (jerseyServlets.length() == 0) {
        log.warn("JETTY: No Jersey Servlets to be configured");
      } else {
        for (String nameAndContext : jerseyServlets.split(",")) {
          String parts[] = nameAndContext.trim().split(":");
          if (parts != null && parts.length > 1) {
            String clazzName = parts[0].trim();
            if (clazzName.length() <= 0) {
              log.error("JETTY: Jersey Servlets malformed: [" + nameAndContext + "]");
              continue;
            }
            String contextPath = parts[1].trim();
            if (contextPath.length() <= 0) {
              log.error("JETTY: Jersey Servlets malformed: [" + nameAndContext + "]");
              continue;
            }
            try {
              log.warn("JETTY: JERSEY SERVLET Located \"" + clazzName + "\"");
              Class clazz = Class.forName(clazzName);
              // Get a SERVLET HOLDER and assign the jersey config setting
              ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
              servletHolder.setInitParameter("com.sun.jersey.config.property.packages", clazz.getPackage().getName());
              // Get a SERVLET context and NOTE that this is where we set the contextPath
              ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
              context.setContextPath(contextPath);
              context.setClassLoader(Thread.currentThread().getContextClassLoader());
              context.addServlet(servletHolder, "/"); // Add it to the context with ROOT as the base
              handlers.addHandler(context); // And add the context to the handlers
              log.warn("JETTY: JERSEY SERVLET " + clazz.getCanonicalName() + " added with context " + contextPath);
            } catch (ClassNotFoundException ex) {
              log.error("JETTY: JERSEY SERVLET Unable to load class: " + clazzName + " - " + ex);
            }
          }
        }
        log.warn("JETTY: Jersey Servlets configured");
      }

      // Error handler must be last in the chain.
      /*
      ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
      errorHandler.addErrorPage(404, "/error.html");
      WebAppContext webAppContext = new WebAppContext();
      webAppContext.setContextPath("/");
      webAppContext.setResourceBase(ejisHome + "/deployments/error_app");
      webAppContext.setErrorHandler(errorHandler);
      handlers.addHandler(webAppContext);
      */

      // Start it up!
      server.start();
      server.join();

      // We're done here
      log.warn("Jetty stopped");

      // We are stopped, so check the EJetty options and return if matching
      if (ejettyHandler.reloadPlease) {
        return (1);
      }
      if (!ejettyHandler.restartPlease) {
        break;
      }
      log.warn("Restarting Jetty"); // Nope. So just restart
    }
    return (0);
  }

}
