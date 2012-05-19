package com.alltamasystems.ejr.utils;

import com.alltamasystems.ejr.EJettyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 20/05/12
 * Time: 8:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

  private static Logger log = LoggerFactory.getLogger(Utils.class);

  public static String getCookieValue(Cookie[] cookies,
                                      String cookieName,
                                      String defaultValue) {
    for (int i = 0; i < cookies.length; i++) {
      Cookie cookie = cookies[i];
      if (cookieName.equals(cookie.getName()))
        return (cookie.getValue());
    }
    return (defaultValue);
  }

  public static void failIfNotAPartner(HttpServletRequest request, EJettyResponse json) {
    String partner = Utils.getCookieValue(request.getCookies(), "SITE_PARTNER", "");
    if (!Constants.validPartners.contains(partner)) {
      json.setStatus(false);
      json.addMessage(Constants.INVALID_PARTNER);
      log.error("Invalid partner provided");
    }
  }

  public static void failIfDeniedAccess(HttpServletRequest request, EJettyResponse json) {
    String ipAddress = request.getRemoteAddr();
    if (!Constants.validExternalIPAddresses.contains(ipAddress)) {
      json.setStatus(false);
      json.addMessage(Constants.DENIED_ACCESS);
      log.error("Invalid ip address " + ipAddress);
    }
  }

  public static void failIfWrongCountry(HttpServletRequest request, EJettyResponse json, String country) {
    if (!Constants.validCountries.contains(country)) {
      json.setStatus(false);
      json.addMessage(Constants.INVALID_COUNTRY);
      log.error("Invalid ip address " + country);
    }
  }

  public static String MD5(String str) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] array = md.digest(str.getBytes(Charset.forName("UTF-8")));
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i) {
        sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
      }
      return sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
    }
    return null;
  }

  public static boolean isValidEmail(String email) {
    if (!isValidString(email)) return false;
    String emailRE = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$/";
    return (email.matches(emailRE));
  }

  public static boolean isValidString(String str) {
    if (str == null || str.trim().isEmpty()) return false;
    return true;
  }

  public static boolean isValidCountry(String country) {
    if (!isValidString(country)) return false;
    if (!Constants.validCountries.contains(country)) return false;
    return true;
  }

  public static boolean isValidDateOfBirth(String date_of_birth) {
    if (!isValidString(date_of_birth)) return false;
    String dateRE = "^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$";
    if (!date_of_birth.matches(dateRE)) return false;
    try {
      DateFormat formatter = new SimpleDateFormat("YYYY-MM-dd");
      Date date = (Date) formatter.parse(date_of_birth);
    } catch (ParseException ex) {
      return false;
    }
    return true;
  }

  public static boolean isValidGender(String gender) {
    if (!isValidString(gender)) return false;
    return (gender.equals("M") || gender.equals("F") || gender.equals("X"));
  }

  public static boolean isTF(String tf) {
    if (!isValidString(tf)) return false;
    return (tf.toUpperCase().equals("T") || tf.toUpperCase().equals("F") || tf.toUpperCase().equals("Y") || tf.toUpperCase().equals("N") || tf.toUpperCase().equals("1") || tf.toUpperCase().equals("0"));
  }

  public static boolean toTF(String tf) {
    if (!isValidString(tf)) return false;
    return (tf.toUpperCase().equals("T") || tf.toUpperCase().equals("Y") || tf.toUpperCase().equals("1"));
  }

}
