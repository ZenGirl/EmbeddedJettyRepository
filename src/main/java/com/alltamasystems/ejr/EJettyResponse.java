package com.alltamasystems.ejr;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 18/05/12
 * Time: 2:14 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlType(propOrder = {"status","messages","value"})
public class EJettyResponse {


  private boolean status = true;
  private List<String> messages = new ArrayList<String>();
  private JSONArray value = new JSONArray();

  private static Logger log = LoggerFactory.getLogger(EJettyResponse.class);

  public EJettyResponse() {

  }

  public String toString() {
    JSONObject o = new JSONObject();
    try {
      o.put("status", status);
      o.put("messages", messages);
      o.put("value", value);
      return o.toString(2);
    } catch (JSONException e) {
      log.error("Unable to serialize JsonResponse! " + e);
      e.printStackTrace();
    }
    return o.toString() + "\n\n";
  }

  public void fromString(String jsonResponse) {
    try {
      JSONObject obj = new JSONObject(jsonResponse);
      this.status = obj.getBoolean("status");
      JSONArray jsonArray = obj.getJSONArray("messages");
      for (int i=0; i<jsonArray.length(); i++) {
        this.messages.add(jsonArray.getString(i));
      }
      jsonArray = obj.getJSONArray("value");
      for (int i=0; i<jsonArray.length(); i++) {
        this.value.put(jsonArray.getJSONObject(i));
      }
    } catch (JSONException ex) {
      log.error("Unable to parse incoming string [" + jsonResponse + "]: " + ex);
    }
  }

  public void addMessage(String message) {
    messages.add(message);
    status = false;
    value = new JSONArray();
  }

  public boolean failed() {
    return status == false;
  }

  public boolean isStatus() {
    return status;
  }

  public boolean getStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public List<String> getMessages() {
    return messages;
  }

  public void setMessages(List<String> messages) {
    this.messages = messages;
  }

  public void addValue(JSONObject value) {
    this.value.put(value);
  }

  public JSONArray getValue() {
    return value;
  }

  public void setValue(JSONArray value) {
    this.value = value;
  }
}
