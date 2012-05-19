package com.alltamasystems.ejr;

import org.apache.log4j.*;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 18/05/12
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class EJettyLogging {
  private static String SITE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss,SSS}{UTC} SITE  %-5p [%-20t] %c{1.} - %m%n";
  private static String JETTY_PATTERN = "%d{yyyy-MM-dd HH:mm:ss,SSS}{UTC} JETTY %-5p [%-20t] %c{1.} - %m%n";
  private static String LOG_FOLDER = "logs";
  private String env;
  private boolean runningInDevelopment = false;
  private String runtimeFolder = ".";

  public EJettyLogging(String env, String runtimeFolder) {
    this.env = env;
    runningInDevelopment = (env.equals("dev"));
    this.runtimeFolder = runtimeFolder;
  }

  public void configure() {
    if (runningInDevelopment)
      configureDevelopment();
    else
      configureProduction();
  }

  private void configureDevelopment() {
    System.out.println("EJettyLogging: Configuring for Development");
    System.out.println("EJettyLogging: Configuring rootLogger");
    Logger rootLogger = Logger.getRootLogger();
    rootLogger.setLevel(Level.DEBUG);
    rootLogger.removeAllAppenders();

    System.out.println("EJettyLogging: Configuring JETTY Console appender");
    ConsoleAppender jettyAppender = new ConsoleAppender();
    jettyAppender.setLayout(new EnhancedPatternLayout(JETTY_PATTERN));
    jettyAppender.activateOptions();
    rootLogger.addAppender(jettyAppender);

    System.out.println("EJettyLogging: Configuring SITE  Console appender");
    ConsoleAppender siteAppender = new ConsoleAppender();
    siteAppender.setLayout(new EnhancedPatternLayout(SITE_PATTERN));
    siteAppender.activateOptions();
    rootLogger.addAppender(siteAppender);

    System.out.println("EJettyLogging: Configuring com.alltamasystems logger");
    Logger cgLogger = rootLogger.getLoggerRepository().getLogger("com.alltamasystems");
    cgLogger.setLevel(Level.INFO);
    cgLogger.setAdditivity(false);
    cgLogger.removeAllAppenders();
    cgLogger.addAppender(siteAppender);

    System.out.println("EJettyLogging: Configuring org.eclipse logger");
    Logger jettyLogger = rootLogger.getLoggerRepository().getLogger("org.eclipse");
    jettyLogger.setLevel(Level.WARN);
    jettyLogger.setAdditivity(false);
    jettyLogger.removeAllAppenders();
    jettyLogger.addAppender(jettyAppender);
  }

  public void configureProduction() {
    System.out.println("EJettyLogging: Configuring for Production");

    String appLogFolder = runtimeFolder + "/" + LOG_FOLDER;
    File f = new File(appLogFolder);
    if (! f.exists()) {
      System.out.println("EJettyLogging: " + appLogFolder + " not found - attempting creation");
      f.mkdir();
      if (! f.exists()) {
        System.out.println("EJettyLogging: Unable to create " + appLogFolder);
        fallBackToConsoleLogging(appLogFolder);
      }
    }
    if (! f.canWrite())
      fallBackToConsoleLogging(appLogFolder);

    System.out.println("EJettyLogging: Configuring rootLogger");
    Logger rootLogger = Logger.getRootLogger();
    rootLogger.setLevel(Level.DEBUG);
    rootLogger.removeAllAppenders();

    System.out.println("EJettyLogging: Configuring JETTY RollingFile appender");
    Appender jettyAppender = getDailyRollingFileAppenderAppender(JETTY_PATTERN);
    rootLogger.addAppender(jettyAppender);

    System.out.println("EJettyLogging: Configuring SITE  RollingFile appender");
    Appender siteAppender = getDailyRollingFileAppenderAppender(SITE_PATTERN);
    rootLogger.addAppender(siteAppender);

    System.out.println("EJettyLogging: Configuring com.alltamasystems logger INFO");
    Logger cgLogger = rootLogger.getLoggerRepository().getLogger("com.alltamasystems");
    cgLogger.setLevel(Level.WARN);
    cgLogger.setAdditivity(false);
    cgLogger.removeAllAppenders();
    cgLogger.addAppender(siteAppender);

    System.out.println("EJettyLogging: Configuring org.eclipse logger WARN");
    Logger jettyLogger = rootLogger.getLoggerRepository().getLogger("org.eclipse");
    jettyLogger.setLevel(Level.WARN);
    jettyLogger.setAdditivity(false);
    jettyLogger.removeAllAppenders();
    jettyLogger.addAppender(jettyAppender);
  }

  public void changeLevel(Level level) {
    System.out.println("EJettyLogging: Configuring com.alltamasystems logger " + level.toString());
    Logger rootLogger = Logger.getRootLogger();
    Logger cgLogger = rootLogger.getLoggerRepository().getLogger("com.alltamasystems");
    cgLogger.setLevel(level);
  }

  private void fallBackToConsoleLogging(String folder) {
    System.out.println("*******************************************************");
    System.out.println("* ERROR! ${folder} is not found or not writable");
    System.out.println("* CONFIGURING CONSOLE LOGGING");
    System.out.println("*******************************************************");
    configureDevelopment();
  }

  private DailyRollingFileAppender getDailyRollingFileAppenderAppender(String pattern) {
    DailyRollingFileAppender appender = new DailyRollingFileAppender();
    appender.setAppend(true);
    appender.setDatePattern("'.'yyyy-MM-dd");
    appender.setLayout(new EnhancedPatternLayout(pattern));
    appender.setFile(runtimeFolder + "/" + LOG_FOLDER + "/app.log");
    appender.activateOptions();
    return appender;
  }

}
