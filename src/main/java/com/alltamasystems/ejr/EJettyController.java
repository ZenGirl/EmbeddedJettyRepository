package com.alltamasystems.ejr;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 18/05/12
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class EJettyController extends AbstractHandler {
  private static Logger log = LoggerFactory.getLogger(EJettyController.class);

  private Server server = null; // The jetty server instance passed in
  public boolean restartPlease = false; // If true, simply restarts the server
  public boolean reloadPlease = false; // If true, forces complete reload
  public boolean stopPlease = false;
  public String ejettySecret = "bf7d0eaaec64b80bcd09d6f132ecb567"; // The default secret for urls. (EJettyRocks)

  public EJettyController(Server server, String ejettySecret) {
    this.server = server;
    this.ejettySecret = ejettySecret;
  }

  public void handle(String string, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    baseRequest.setHandled(true);
    log.warn("EJetty request received");
    if (pathMatchedControls(request, response)) {
      log.warn("EJetty request completed");
      return;
    }
    log.warn("EJetty invalid request");
    String url = request.getParameter("url");
    String msg = "Unable to understand the URI: [" + url + "]";
    log.error(msg);
    response.sendError(500, msg);
  }

  public boolean pathMatchedControls(HttpServletRequest request, HttpServletResponse response) throws IOException {
    boolean result = false;
    String pathInfo = request.getPathInfo();
    String querySecret = request.getParameter("secret");
    if (!checkSecret(querySecret)) {
      return false;
    } else if ("/stop".equals(pathInfo)) {
      // Just stop the server
      result = stop(response);
      stopPlease = true;
    } else if ("/restart".equals(pathInfo)) {
      // Set restart option and stop the server
      restartPlease = true;
      result = stop(response);
    } else if ("/reload".equals(pathInfo)) {
      // Set reload option and stop the server
      reloadPlease = true;
      result = stop(response);
    } else if ("/applog".equals(pathInfo)) {
      response.setStatus(200);
      response.setContentType("text/plain");
      ServletOutputStream os = response.getOutputStream();
      String appLog = "logs/app.log";
      os.println(appLog);
      File f = new File(appLog);
      if (f.exists()) {
        try {
          BufferedReader in = new BufferedReader(new FileReader(appLog));
          String str;
          while ((str = in.readLine()) != null) {
            os.println(str);
          }
          in.close();
        } catch (IOException e) {
          log.error("JETTY: Error reading " + appLog + e);
        }
      } else {
        os.println("ERROR: Not found");
      }
      os.close();
      result = true;
    } else if ("/status".equals(pathInfo)) {
      response.setStatus(200);
      response.setContentType("text/plain");
      ServletOutputStream os = response.getOutputStream();
      int mb = 1024 * 1024;
      os.println("JVM Memory Total (Mb): " + (Runtime.getRuntime().totalMemory() / mb));
      os.println("JVM Memory Free  (Mb): " + (Runtime.getRuntime().freeMemory() / mb));
      os.println("JVM Memory Used  (Mb): " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / mb));
      os.println("JVM Memory Max   (Mb): " + (Runtime.getRuntime().maxMemory() / mb));
      MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
      os.println("Heap Committed   (Mb): " + mxBean.getHeapMemoryUsage().getCommitted() / mb);
      os.println("Heap Max         (Mb): " + mxBean.getHeapMemoryUsage().getMax() / mb);
      os.println("Heap Used        (Mb): " + mxBean.getHeapMemoryUsage().getUsed() / mb);
      ClassLoadingMXBean clBean = ManagementFactory.getClassLoadingMXBean();
      os.println("Classes Loaded       : " + clBean.getLoadedClassCount());
      os.println("Total Classes        : " + clBean.getTotalLoadedClassCount());
      ThreadMXBean thBean = ManagementFactory.getThreadMXBean();
      os.println("Total Threads        : " + thBean.getThreadCount());
      os.close();
      result = true;
    } else if ("/routes".equals(pathInfo)) {
      response.setStatus(200);
      response.setContentType("text/plain");
      ServletOutputStream os = response.getOutputStream();
      os.println("Server State");
      os.println("  Failed:  " + server.isFailed());
      os.println("  Running: " + server.isRunning());
      os.println("  Started: " + server.isStarted());
      os.println("  Attributes:");
      Enumeration e = server.getAttributeNames();
      while (e.hasMoreElements()) {
        String attrName = (String)e.nextElement();
        os.println("    " + attrName + "->" + server.getAttribute(attrName));
      }
      os.println("  Connectors:");
      for (Connector connector : server.getConnectors()) {
        os.println("    Port: " + connector.getPort() + " Name: " + connector.getName() + " LocalPort: " + connector.getLocalPort());
      }
      os.println("  Connectors:");
      for (Handler handler : server.getHandlers()) {
        os.println("    Handler: " + handler.toString());
        if (handler instanceof HandlerCollection) {
          HandlerCollection handlerCollection = (HandlerCollection)handler;
          os.println("      Collection: " + handlerCollection.toString());
          for (Handler handler1 : handlerCollection.getHandlers()) {
            if (handler1 instanceof RequestLogHandler) {
              os.println("      -> RequestLogHandler: " + ((RequestLogHandler)handler1).toString());
            } else if (handler1 instanceof ContextHandler) {
              ContextHandler contextHandler = (ContextHandler)handler1;
              os.println("      -> ContextHandler: " + contextHandler.getContextPath());
              os.println("        -> ContextPath:  " + contextHandler.getContextPath());
              os.println("        -> ResourceBase: " + contextHandler.getResourceBase());
              os.println("        -> State:        " + contextHandler.getState());
              os.println("        -> Available:    " + contextHandler.isAvailable());
              os.println("        -> Running:      " + contextHandler.isRunning());
            } else if (handler1 instanceof WebAppContext) {
              WebAppContext webAppContext = (WebAppContext)handler1;
              os.println("      -> WebAppContext: " + webAppContext.toString());
            } else {
              os.println("      -> Handler: " + handler1.toString());
            }
          }
        }
      }
      os.close();
      result = true;
    }
    return result;
  }

  private boolean checkSecret(String querySecret) {
    if (querySecret == null || !this.ejettySecret.equals(querySecret)) {
      log.warn("Invalid secret provided. Ignoring request");
      return false;
    }
    return true;
  }

  private boolean stop(HttpServletResponse response) throws IOException {
    log.warn("Stopping Jetty");
    response.setStatus(202);
    response.setContentType("text/plain");
    ServletOutputStream os = response.getOutputStream();
    os.println("Shutting down.");
    os.close();
    response.flushBuffer();
    new Thread() {

      @Override
      public void run() {
        log.info("Shutting down Jetty...");
        try {
          server.stop();
        } catch (Exception e) {
          log.error("Problem stopping Jetty instance: " + e);
        }
        log.info("Jetty has stopped.");
      }
    }.start();
    return true;
  }

}



