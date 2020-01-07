package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.TransportApi;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 12/12/17
 * Time: 9:28 AM
 */
public interface Authenticator extends TransportApi {

    /**
     * Fetches the auth-token from Anaplan Authentication API
     *
     * @return Auth-token for the user-session
     */
    String getAuthToken();

    /**
     * Fetches the authentication client
     *
     * @return
     */
    AnaplanAuthenticationAPI getAuthClient();

    /**
     * Sets a custom authenticator for testing purposes
     *
     * @param authClient
     */
    void setAuthClient(AnaplanAuthenticationAPI authClient);

    /**
     * Performs authentication for the appropriate authentication mechanism
     *
     * @return
     */
    byte[] authenticate();
}
