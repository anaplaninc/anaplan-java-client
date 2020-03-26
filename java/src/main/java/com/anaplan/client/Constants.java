package com.anaplan.client;

public class Constants {

    /**
     * Part of the path for the password file
     */
    public static final String PW_FILE_PATH_SEGMENT = ".anaplan/api-client/keystore-access.txt";

    public static final int AC_MAJOR = 1;
    public static final int AC_MINOR = 4;
    public static final int AC_REVISION = 4 ;
    public static final String AC_Release = "-Snapshot";

    public static final boolean AUTH_CLIENT_CACHE_ENABLED = false;

    public static final Integer AUTH_TTL_SECONDS = 30;

    public static final String X_ACONNECT_HEADER_KEY = "X-AConnect-Client";
    public static final String X_ACONNECT_HEADER_VALUE = "Anaplan_Connect_1.4.4";
    public static final String X_ACONNECT_HEADER = X_ACONNECT_HEADER_KEY + ":" + X_ACONNECT_HEADER_VALUE;

    public static final String CORS_HEADER_KEY = "Origin";
    public static final String CORS_HEADER_VALUE = "https://www.anaplan.com";

    public static final int MIN_RETRY_COUNT = 3;
    public static final int MAX_RETRY_COUNT = 15;

    public static final int MIN_RETRY_TIMEOUT_SECS = 3;
    public static final int MAX_RETRY_TIMEOUT_SECS = 60;

    public static final int MIN_HTTP_CONNECTION_TIMEOUT_SECS = 3;
    public static final int MAX_HTTP_CONNECTION_TIMEOUT_SECS = 60;

    public static final double DEFAULT_BACKOFF_MULTIPLIER = 1.5;
}
