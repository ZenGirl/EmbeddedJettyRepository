package com.alltamasystems.ejr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 18/05/12
 * Time: 1:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class EJettyMain {

  private static int parseDefault(String varName, String originalValue, int defaultValue) {
    String original = originalValue;
    int result = defaultValue;
    if (original == null || original.trim().isEmpty()) {
      System.out.println("EJettyMain: WARNING! No -D" + varName + "=" + original + " provided! Defaulting to " + defaultValue);
      original = "" + defaultValue;
    }
    try {
      result = Integer.parseInt(original);
    } catch (NumberFormatException ex) {
      System.out.println("EJettyMain: WARNING! Invalid integer -D" + varName + "=" + original + " provided! Defaulting to " + defaultValue);
      result = defaultValue;
    }
    return result;
  }

  public static void main(String[] args) throws Exception {

    // Get the command line options
    String respondOn = System.getProperty("respondOn");
    String env = System.getProperty("env");
    String minThreads = System.getProperty("minThreads");
    String maxThreads = System.getProperty("maxThreads");

    // Special option for specifying runtime folder
    String runtimeFolder = System.getProperty("runtimeFolder");
    if (runtimeFolder == null) {
      for (String listArg : Arrays.asList(args)) {
        if (listArg.startsWith("runtimeFolder")) {
          String[] parts = listArg.split("=");
          if (parts.length == 2) {
            runtimeFolder = parts[1].trim();
          }
        }
      }
    }

    // Our defaults
    String ipAddress = "0.0.0.0";
    int port = 8080;

    // Validate the respondOn
    if (respondOn == null || respondOn.trim().isEmpty()) {
      System.out.println("EJettyMain: WARNING! No -DrespondOn=x.x.x.x:port provided! Defaulting to 0.0.0.0:8080");
    } else {
      String[] respondOnParts = respondOn.split(":");
      if (respondOnParts.length < 2) {
        System.out.println("EJettyMain: WARNING! No -DrespondOn=x.x.x.x:port provided! Defaulting to 0.0.0.0:8080");
      } else {
        if (respondOnParts[0].trim().isEmpty() || respondOnParts[1].trim().isEmpty()) {
          System.out.println("EJettyMain: WARNING! No -DrespondOn=x.x.x.x:port provided! Defaulting to 0.0.0.0:8080");
        } else {
          ipAddress = respondOnParts[0];
          port = parseDefault("port", respondOnParts[1], 8080);
        }
      }
    }

    // Validate the environment
    if (env == null || env.trim().isEmpty()) {
      System.out.println("EJettyMain: WARNING! No -Denv=(dev|prod) provided! Defaulting to dev");
      env = "dev";
    }
    if (!env.equals("dev") && !env.equals("prod")) {
      System.out.println("EJettyMain: WARNING! No -Denv=(dev|prod) provided! Defaulting to dev");
      env = "dev";
    }

    // Get the min and max threads
    int minThreadsNumber = parseDefault("minThreads", minThreads, 5);
    int maxThreadsNumber = parseDefault("maxThreads", maxThreads, 50);

    // Configure logging
    new EJettyLogging(env, runtimeFolder).configure();

    // And then configure a logger
    Logger log = LoggerFactory.getLogger(EJettyMain.class);

    // Run the app
    log.warn("EJettyMain: Starting main application");
    EJettyRunner runner = new EJettyRunner(env, ipAddress, port, minThreadsNumber, maxThreadsNumber, runtimeFolder);
    int returnCode = runner.start();

    // And return the code
    log.warn("EJettyMain: Main application returned " + returnCode);
    System.exit(returnCode);

  }

}
