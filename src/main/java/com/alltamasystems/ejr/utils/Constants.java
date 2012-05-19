package com.alltamasystems.ejr.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 20/05/12
 * Time: 8:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class Constants {

  public static List<String> validCountries = new ArrayList<String>();

  static {
    validCountries.add("AU");
    validCountries.add("US");
  }

  public static List<String> validExternalIPAddresses = new ArrayList<String>();

  static {
    validExternalIPAddresses.add("203.149.80.98");
    validExternalIPAddresses.add("127.0.0.1");
  }

  public static List<String> validPartners = new ArrayList<String>();

  static {
    validPartners.add("Website");
    validPartners.add("Mobile");
  }

  public static String INVALID_COUNTRY = "9001:Invalid Country";
  public static String INVALID_PARTNER = "9002:Invalid Partner";
  public static String DENIED_ACCESS = "9003:Denied Access";
  public static String INVALID_USERNAME = "9004:Invalid username";
  public static String INVALID_EMAIL = "9005:Invalid email";
  public static String MISSING_FULL_NAME = "9006:Missing full_name";
  public static String MISSING_PASSWORD = "9007:Missing password";
  public static String INVALID_DATE_OF_BIRTH = "9008:Invalid date of birth";
  public static String INVALID_GENDER = "9009:Invalid gender";
  public static String INVALID_ALLOW_EXPLICIT = "9010:Invalid allow_explicit";
  public static String INVALID_ACTIVE = "9011:Invalid active";
  public static String INVALID_ID = "9012:Invalid id";
  public static String NOT_LOGGED_IN = "9013:Not logged in";

}
