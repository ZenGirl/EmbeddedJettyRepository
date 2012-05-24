package com.alltamasystems.ejr.maven.simpleHandler;

import com.alltamasystems.ejr.EJettyResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 24/05/12
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleAPI extends AbstractHandler {

  private Logger log = LoggerFactory.getLogger(SimpleAPI.class);

  public void handle(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String method = request.getMethod();
    log.info("Received " + method + ":" + path);
    if ("GET".equals(method)) {
      doGet(path, baseRequest, request, response);
    } else if ("POST".equals(method)) {
      doPost(path, baseRequest, request, response);
    } else if ("PUT".equals(method)) {
      doPut(path, baseRequest, request, response);
    } else if ("DELETE".equals(method)) {
      doDelete(path, baseRequest, request, response);
    }
  }

  public void doGet(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // Say something about the call
    log.debug("-> Handling a GET for " + path);

    // If we set this false, then the whole action actis like a filter...
    baseRequest.setHandled(true);

    // Now setup a writer.
    PrintWriter out = response.getWriter();
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    out.write(justBuildSomeExampleOutput());
    out.close();
  }

  public void doPost(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // Say something about the call
    log.debug("-> Handling a POST for " + path);

    // If we set this false, then the whole action acts like a filter...
    baseRequest.setHandled(true);

    // Now setup a writer.
    PrintWriter out = response.getWriter();
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    out.write(justBuildSomeExampleOutput());
    out.close();
  }

  public void doPut(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // Say something about the call
    log.debug("-> Handling a PUT for " + path);

    // If we set this false, then the whole action acts like a filter...
    baseRequest.setHandled(true);

    // Now setup a writer.
    PrintWriter out = response.getWriter();
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    out.write(justBuildSomeExampleOutput());
    out.close();
  }

  public void doDelete(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // Say something about the call
    log.debug("-> Handling a DELETE for " + path);

    // If we set this false, then the whole action acts like a filter...
    baseRequest.setHandled(true);

    // Now setup a writer.
    PrintWriter out = response.getWriter();
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    out.write(justBuildSomeExampleOutput());
    out.close();
  }

  private String justBuildSomeExampleOutput() {
    EJettyResponse r = new EJettyResponse();
    r.setStatus(true);
    r.addMessage("Worked");
    JSONArray jsonArray = new JSONArray();
    try {
      JSONObject o = new JSONObject();
      o.put("String1", "Hello");
      jsonArray.put(o);
      o.put("String2", "World");
      jsonArray.put(o);
      o.put("Boolean", false);
      jsonArray.put(o);
    } catch (JSONException ex) {
      log.error("Whoops. I should probably do something more reasonable here...");
      ex.printStackTrace();
    }
    r.setValue(jsonArray);
    return r.toString() + "\n";
  }

}
