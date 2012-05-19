package com.alltamasystems.ejr.examples;

import com.alltamasystems.ejr.EJettyResponse;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Servlet2 extends HttpServlet {

  private static Logger log = LoggerFactory.getLogger(Servlet2.class);

  public Servlet2() {
    log.warn("Instance of Servlet2 created");
  }

  protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    log.warn("Inside Servlet2");
    log.info("Sending some basic JSON back");
    EJettyResponse jsonResponse = new EJettyResponse();
    jsonResponse.addMessage("Just a demo message");
    JSONObject o = new JSONObject();
    try {
      o.put("item3", "item3");
      o.put("item4", "item4");
    } catch (JSONException ex) {
      log.error("Something went badly wrong assigning to the JSONObject");
    }
    jsonResponse.setStatus(true);
    jsonResponse.addValue(o);
    log.info("Sending back: " + jsonResponse.toString());
    ServletOutputStream os = response.getOutputStream();
    os.println(jsonResponse.toString());
    os.close();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }
}
