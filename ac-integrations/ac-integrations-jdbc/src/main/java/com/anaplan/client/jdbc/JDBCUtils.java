package com.anaplan.client.jdbc;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

/**
 * Jdbc related utilities, e.g. connection string checks
 */
public class JDBCUtils {

  private final static Pattern QUERY_INTERCEPTOR = Pattern
      .compile("QUERYINTERCEPTOR=", Pattern.CASE_INSENSITIVE);
  private final static Pattern AUTO_DESERIALIZE = Pattern
      .compile("AUTODESERIALIZE=TRUE", Pattern.CASE_INSENSITIVE);

  private static final int MAX_ALLOWED_CONNECTION_STRING_LENGTH = 1500;

  //Set to false positive
  public static void validateURL(final JDBCConfig config) {
    final String url =
        config.getJdbcConnectionUrl() == null ? null : config.getJdbcConnectionUrl().toUpperCase();
    if (url == null) {
      throw new InvalidParameterException(
          "JDBC connection string cannot be empty !");
    }
    if (url.length() > MAX_ALLOWED_CONNECTION_STRING_LENGTH) {
      throw new InvalidParameterException(
          "JDBC connection string cannot be more than " + MAX_ALLOWED_CONNECTION_STRING_LENGTH
              + " characters in length!");
    }
    //Injection vulnerability
    if (QUERY_INTERCEPTOR.matcher(url).find() || AUTO_DESERIALIZE.matcher(url).find()) {
      throw new InvalidParameterException(
          "JDBC connection string is not valid !");
    }
    if (config.getJdbcPassword() == null || config.getJdbcPassword().length == 0) {
      throw new InvalidParameterException(
          "JDBC password string cannot be empty !");
    }

  }

}
