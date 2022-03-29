package com.anaplan.client;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Throwables {

  private Throwables(){}

  /**
   * Returns a string containing the result of {@link Throwable#toString() toString()}, followed by
   * the full, recursive stack trace of {@code throwable}. Note that you probably should not be
   * parsing the resulting string; if you need programmatic access to the stack frames, you can call
   * {@link Throwable#getStackTrace()}.
   */
  public static String getStackTraceAsString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

}
