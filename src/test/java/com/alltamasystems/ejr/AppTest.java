package com.alltamasystems.ejr;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.junit.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 18/05/12
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class AppTest {

  // We're forcing the server to start on port 8080 for testing purposes
  private static String BASE_URL = "http://localhost:8080";
  // Here's the kill command
  private static String STOP_SERVER_URL = BASE_URL + "/ejr/stop?secret=bf7d0eaaec64b80bcd09d6f132ecb567";

  @BeforeClass
  public static void setUpClass() throws Exception {
    Thread startupThread = new Thread() {

      @Override
      public void run() {
        try {
          System.out.println("Starting Jetty...");
          // We'll be forcing a bunch of settings that would normally be done on the command line.
          String[] cmdLineArgs = new String[]{
              "runtimeFolder=" + new File(".").getCanonicalPath() + "/src/test/runtime"
          };
          EJettyMain.main(cmdLineArgs);
        } catch (Exception ex) {
          System.err.println("Error Starting Jetty: " + ex);
        }
      }
    };
    startupThread.start();
    System.out.println("Waiting a few seconds to ensure Jetty is started");
    Thread.sleep(2000);
    System.out.println("Ok. Starting tests");
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    ClientConfig config = new DefaultClientConfig();
    Client client = Client.create(config);
    WebResource service = client.resource(UriBuilder.fromUri(STOP_SERVER_URL).build());
    service.get(String.class);
    System.out.println("Sent stop command");
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * This is just to save verbiage during tests...
   *
   * @param url
   * @return
   */
  private WebResource getService(String url) {
    ClientConfig config = new DefaultClientConfig();
    Client client = Client.create(config);
    // Notice we're setting follow redirects to false
    client.setFollowRedirects(false);
    return client.resource(UriBuilder.fromUri(url).build());
  }

  /**
   * Same as getService but follow redirects
   *
   * @param url
   * @return
   */
  private WebResource getServiceAllowRedirects(String url) {
    ClientConfig config = new DefaultClientConfig();
    Client client = Client.create(config);
    // Notice we're setting follow redirects to true
    client.setFollowRedirects(true);
    return client.resource(UriBuilder.fromUri(url).build());
  }

  /**
   * Just for dumping output responses.
   *
   * @param response
   */
  private void dumpResponse(ClientResponse response) {
    System.out.println("Status: (" + response.getStatus() + ")");
    System.out.println("Headers");
    MultivaluedMap headerMap = response.getHeaders();
    for (Object key : headerMap.keySet()) {
      System.out.println("->" + ((key == null) ? "" : key + ": ") + headerMap.get(key));
    }
    System.out.println("Body");
    System.out.println(response.getEntity(String.class));
  }

  @Test
  @Ignore
  public void testPublicXHTML() {
    System.out.println("--> TEST: Public returns x.html");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/static/x.html").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    String body = response.getEntity(String.class);
    org.junit.Assert.assertEquals("HTML Response:", "<html>\n" + "  <head>\n" + "    <title>x.html</title>\n" + "  </head>\n" + "  <body>\n"
        + "    <h1>Header 1</h1>\n" + "    <p>Bunch of html</p>\n" + "  </body>\n" + "</html>", body);
    System.out.println("    WORKED!");
  }

  @Test
  @Ignore
  public void testEjrStatus() {
    System.out.println("--> TEST: EJR Status returns correctly");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/ejr/status?secret=bf7d0eaaec64b80bcd09d6f132ecb567").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    String body = response.getEntity(String.class);
    org.junit.Assert.assertNotNull(body);
    String[] parts = body.split("\n");
    org.junit.Assert.assertEquals("Size", 10, parts.length);
    org.junit.Assert.assertTrue("JVM Memory Total", parts[0].startsWith("JVM Memory Total"));
    org.junit.Assert.assertTrue("JVM Memory Free", parts[1].startsWith("JVM Memory Free"));
    org.junit.Assert.assertTrue("JVM Memory Used", parts[2].startsWith("JVM Memory Used"));
    org.junit.Assert.assertTrue("JVM Memory Max", parts[3].startsWith("JVM Memory Max"));
    org.junit.Assert.assertTrue("Heap Committed", parts[4].startsWith("Heap Committed"));
    org.junit.Assert.assertTrue("Heap Max", parts[5].startsWith("Heap Max"));
    org.junit.Assert.assertTrue("Heap Used", parts[6].startsWith("Heap Used"));
    org.junit.Assert.assertTrue("Classes Loaded", parts[7].startsWith("Classes Loaded"));
    org.junit.Assert.assertTrue("Total Classes", parts[8].startsWith("Total Classes"));
    org.junit.Assert.assertTrue("Total Threads", parts[9].startsWith("Total Threads"));
    System.out.println("    WORKED!");
  }

  @Test
  @Ignore
  public void testEjrRoutes() {
    System.out.println("--> TEST: EJR Routes returns correctly");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/ejr/routes?secret=bf7d0eaaec64b80bcd09d6f132ecb567").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    String body = response.getEntity(String.class);
    org.junit.Assert.assertNotNull(body);
    System.out.println(body);
/*
Server State
  Failed:  false
  Running: true
  Started: true
  Attributes:
  Connectors:
    Port: 8080 Name: 0.0.0.0:8080 LocalPort: 8080
  Connectors:
    Handler: org.eclipse.jetty.server.handler.HandlerCollection@4090c06f#STARTED
      Collection: org.eclipse.jetty.server.handler.HandlerCollection@4090c06f#STARTED
      -> RequestLogHandler: org.eclipse.jetty.server.handler.RequestLogHandler@5f49d886#STARTED
      -> ContextHandler: /ejr
        -> ContextPath:  /ejr
        -> ResourceBase: null
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
      -> ContextHandler: /static
        -> ContextPath:  /static
        -> ResourceBase: null
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
      -> ContextHandler: /www
        -> ContextPath:  /www
        -> ResourceBase: file:/Users/kim/Documents/Projects/EmbeddedJettyRunner/src/test/runtime/www/
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
      -> ContextHandler: /handler1
        -> ContextPath:  /handler1
        -> ResourceBase: null
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
      -> ContextHandler: /handler2
        -> ContextPath:  /handler2
        -> ResourceBase: null
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
      -> ContextHandler: /
        -> ContextPath:  /
        -> ResourceBase: null
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
      -> ContextHandler: /
        -> ContextPath:  /
        -> ResourceBase: null
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
      -> ContextHandler: /jersey1
        -> ContextPath:  /jersey1
        -> ResourceBase: null
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
      -> ContextHandler: /jersey2
        -> ContextPath:  /jersey2
        -> ResourceBase: null
        -> State:        STARTED
        -> Available:    true
        -> Running:      true
 */
    System.out.println("    WORKED!");
  }

  @Test
  @Ignore
  public void testPublicXTXT() {
    System.out.println("--> TEST: Public returns x.txt");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/static/x.txt").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    String body = response.getEntity(String.class);
    org.junit.Assert.assertEquals("HTML Response:", "This is the file x.txt\n", body);
    System.out.println("    WORKED!");
  }

  @Test
  @Ignore
  public void testPublicNotFound() {
    System.out.println("--> TEST: Public does not return z.txt");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/static/z.txt").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 404) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 404", 404, response.getStatus());
    System.out.println("    WORKED!");
  }

  @Test
  @Ignore
  public void testHandler1WithTrailingSlash() {
    System.out.println("--> TEST: Handler1 with trailing slash returns correct response");
    ClientResponse response = null;
    try {
      response = getServiceAllowRedirects(BASE_URL + "/handler1/").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());

    String body = response.getEntity(String.class);
//    System.out.println("Received: [" + body + "]");

    EJettyResponse jsonResponse = new EJettyResponse();
    jsonResponse.fromString(body);

    org.junit.Assert.assertEquals("Status is true", true, jsonResponse.getStatus());
    System.out.println("    WORKED!");
  }

  @Test
  @Ignore
  public void testHandler1WithoutTrailingSlash() {
    System.out.println("--> TEST: Handler1 without trailing slash returns correct response");
    ClientResponse response = null;
    try {
      response = getServiceAllowRedirects(BASE_URL + "/handler1").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    String body = response.getEntity(String.class);
//    System.out.println("Received: [" + body + "]");
    EJettyResponse jsonResponse = new EJettyResponse();
    jsonResponse.fromString(body);
    org.junit.Assert.assertEquals("Status is true", true, jsonResponse.getStatus());
    System.out.println("    WORKED!");
  }

  @Test
  @Ignore
  public void testHandler2WithTrailingSlash() {
    System.out.println("--> TEST: Handler2 with trailing slash returns correct 200");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/handler2/?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
  }

  @Test
  @Ignore
  public void testHandler2WithoutTrailingSlash() {
    System.out.println("--> TEST: Handler2 without trailing slash returns correct 302");
    ClientResponse response = null;
    try {
      // Note we don't have a trailing slash and we're not following redirects
      response = getService(BASE_URL + "/handler2?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    // Therefore we expect to see a 302
    if (response.getStatus() != 302) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    //dumpResponse(response);
    org.junit.Assert.assertEquals("Response is 302", 302, response.getStatus());
    try {
      MultivaluedMap headers = response.getHeaders();
      List items = (List) headers.get("Location");
      org.junit.Assert.assertEquals("Only one location header", 1, items.size());
      // Here is what we expect to see in the location header.
      org.junit.Assert.assertEquals("Location is http://nowhere2.com", "http://localhost:8080/handler2/?url=hello", items.get(0));
      System.out.println("    WORKED!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // ------------------------------------------------------------------------
  // SERVLET1
  // ------------------------------------------------------------------------
  @Test @Ignore
  public void testServlet1WithTrailingSlashWithoutRedirects() {
    System.out.println("--> TEST: Servlet1 with trailing slash and no redirects returns correct 404");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/servlet1/?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 404) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 404", 404, response.getStatus());
    System.out.println("    WORKED!");
  }

  @Test @Ignore
  public void testServlet1WithTrailingSlashWithRedirects() {
    System.out.println("--> TEST: Servlet1 with trailing slash with redirects returns correct 404");
    ClientResponse response = null;
    try {
      response = getServiceAllowRedirects(BASE_URL + "/servlet1/?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 404) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 404", 404, response.getStatus());
    System.out.println("    WORKED!");
  }

  @Test @Ignore
  public void testServlet1WithoutTrailingSlashWithoutRedirects() {
    System.out.println("--> TEST: Servlet1 without trailing slash without redirects returns correct 200");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/servlet1?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    System.out.println("    WORKED!");
  }

  @Test @Ignore
  public void testServlet1WithoutTrailingSlashWithRedirects() {
    System.out.println("--> TEST: Servlet1 without trailing slash with redirects returns correct 200");
    ClientResponse response = null;
    try {
      response = getServiceAllowRedirects(BASE_URL + "/servlet1?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    System.out.println("    WORKED!");
  }




  // ------------------------------------------------------------------------
  // SERVLET2
  // ------------------------------------------------------------------------
  @Test @Ignore
  public void testServlet2WithTrailingSlashWithoutRedirects() {
    System.out.println("--> TEST: Servlet2 with trailing slash and no redirects returns correct 404");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/servlet2/?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 404) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 404", 404, response.getStatus());
    System.out.println("    WORKED!");
  }

  @Test @Ignore
  public void testServlet2WithTrailingSlashWithRedirects() {
    System.out.println("--> TEST: Servlet2 with trailing slash with redirects returns correct 404");
    ClientResponse response = null;
    try {
      response = getServiceAllowRedirects(BASE_URL + "/servlet2/?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 404) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 404", 404, response.getStatus());
    System.out.println("    WORKED!");
  }

  @Test @Ignore
  public void testServlet2WithoutTrailingSlashWithoutRedirects() {
    System.out.println("--> TEST: Servlet2 without trailing slash without redirects returns correct 200");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/servlet2?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    System.out.println("    WORKED!");
  }

  @Test @Ignore
  public void testServlet2WithoutTrailingSlashWithRedirects() {
    System.out.println("--> TEST: Servlet2 without trailing slash with redirects returns correct 200");
    ClientResponse response = null;
    try {
      response = getServiceAllowRedirects(BASE_URL + "/servlet2?url=hello").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    System.out.println("    WORKED!");
  }






  // ------------------------------------------------------------------------
  // JERSEY1
  // ------------------------------------------------------------------------
  @Test @Ignore
  public void testJersey1() {
    System.out.println("--> TEST: Jersey1 returns correct 200 and body");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/jersey1/user1/").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    org.junit.Assert.assertEquals("Valid body", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<examplePojo><number>100</number><words>hello world 1</words></examplePojo>", response.getEntity(String.class));
    System.out.println("    WORKED!");
  }


  // ------------------------------------------------------------------------
  // JERSEY2
  // ------------------------------------------------------------------------
  @Test @Ignore
  public void testJersey2() {
    System.out.println("--> TEST: Jersey2 returns correct 200 and body");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/jersey2/user2/").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    org.junit.Assert.assertEquals("Valid body", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<examplePojo><number>100</number><words>hello world 2</words></examplePojo>", response.getEntity(String.class));
    System.out.println("    WORKED!");
  }

  // ------------------------------------------------------------------------
  // JEE webapp
  // ------------------------------------------------------------------------
  @Test
  public void testWebappRawHTML() {
    System.out.println("--> TEST: WebApp /x.html returns correct 200 and body");
    ClientResponse response = null;
    try {
      response = getService(BASE_URL + "/www/x.html").get(ClientResponse.class);
    } catch (Exception e) {
      System.err.println("    FAILED! Jetty not running");
    }
    if (response.getStatus() != 200) {
      System.err.println("    FAILED! Returned status " + response.getStatus());
    }
    org.junit.Assert.assertEquals("Response is 200", 200, response.getStatus());
    org.junit.Assert.assertEquals("HTML Response:", "<html>\n  <head>\n    <title>x.html</title>\n  </head>\n"
        + "  <body>\n    <h1>Header 1</h1>\n    <p>Bunch of html</p>\n  </body>\n</html>", response.getEntity(String.class));
    System.out.println("    WORKED!");
  }


}











































