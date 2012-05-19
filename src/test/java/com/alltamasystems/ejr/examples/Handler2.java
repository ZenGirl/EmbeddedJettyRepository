package com.alltamasystems.ejr.examples;

import com.alltamasystems.ejr.EJettyResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Handler2 extends AbstractHandler {

  private static Logger log = LoggerFactory.getLogger(Handler2.class);

  public Handler2() {
    log.warn("Instance of Handler2 created");
  }

  @Override
  public void handle(String string, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    log.warn("Inside Handler2");
    baseRequest.setHandled(true);
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
}
