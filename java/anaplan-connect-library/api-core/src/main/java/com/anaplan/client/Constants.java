package com.anaplan.client;

public class Constants {

  private Constants(){}

  /**
   * Part of the path for the password file
   */
  public static final String PW_FILE_PATH_SEGMENT = ".anaplan/api-client/keystore-access.txt";

  public static final String DEFAULT_AC_VERSION = "4.0.1";
  public static final String AC_VERSION_PROPERTY_KEY = "ac.version";
  public static final String AC_VERSION = Utils.getPropertiesFromClassPathPomProperties(AC_VERSION_PROPERTY_KEY, DEFAULT_AC_VERSION);
  public static final int AC_MAJOR = Integer.parseInt(String.valueOf(AC_VERSION.split("\\.")[0]));
  public static final int AC_MINOR = Integer.parseInt(String.valueOf(AC_VERSION.split("\\.")[1]));
  public static final int AC_REVISION = Integer.parseInt(String.valueOf(AC_VERSION.split("\\.")[2]));

  public static final String X_ACONNECT_HEADER_KEY = "X-AConnect-Client";
  public static final String X_ACONNECT_HEADER_VALUE = "Anaplan_Connect_"+ AC_VERSION;

  public static final String CORS_HEADER_KEY = "Origin";
  public static final String CORS_HEADER_VALUE = "https://www.anaplan.com";

  public static final int MIN_RETRY_COUNT = 3;
  public static final int MAX_RETRY_COUNT = 15;

  public static final int MIN_RETRY_TIMEOUT_SECS = 3;
  public static final int MAX_RETRY_TIMEOUT_SECS = 120;

  public static final int MIN_HTTP_CONNECTION_TIMEOUT_SECS = 3;
  public static final int MAX_HTTP_CONNECTION_TIMEOUT_SECS = 300;

  public static final double DEFAULT_BACKOFF_MULTIPLIER = 1.5;

  public static final String TIME_DIMENSION = "Time";
  public static final String VERSION_DIMENSION = "Version";
  public static final int MAX_BATCH_SIZE = 100 * 1000;
  public static final int BATCH_SIZE = 75 * 1000;

  public static final String DATA_TYPE = "dataType";
  public static final String BOOLEAN = "boolean";
  public static final String ONE = "1";
  public static final String ZERO = "0";
  public static final String TWO = "2";
}
