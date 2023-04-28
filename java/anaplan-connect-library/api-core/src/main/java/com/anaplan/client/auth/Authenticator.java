package com.anaplan.client.auth;

/**
 * Created by Spondon Saha User: spondonsaha Date: 12/12/17 Time: 9:28 AM
 */
public interface Authenticator {

  /**
   * Fetches the auth-token from Anaplan Authentication API
   *
   * @return Auth-token for the user-session
   */
  String authToken();

  /**
   * Performs authentication for the appropriate authentication mechanism
   *
   * @return {@link byte[]}
   */
  byte[] authenticate();

  void setAuthToken(byte[] authToken);
}
