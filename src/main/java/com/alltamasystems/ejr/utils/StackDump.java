package com.alltamasystems.ejr.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 20/05/12
 * Time: 8:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class StackDump {
  /**
   * Given an exception returns the StackTrace as a string.
   * Returns empty string if exception is null.
   * Format is each line newline separated.
   * All lines except first are preceded by tab.
   *
   * @param aThrowable the exception
   * @return string representation of StackTrace.
   */
  public static String getStackTrace(Throwable aThrowable) {
    if (aThrowable == null)
      return "";
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
}
