package com.anaplan.client.transport;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.apache.http.impl.auth.NTLMEngine;

import java.io.IOException;
import java.util.List;

/**
 * Yanked from here: https://github.com/square/okhttp/issues/206
 * User: spondonsaha
 * Date: 9/20/17
 * Time: 11:41 PM
 */
public class NtlmAuthenticator implements Authenticator {
    private NTLMEngine engine;
    private final String workstation;
    private final String domain;
    private final String username;
    private final String password;
    private final String ntlmMsg1;

    public NtlmAuthenticator(String username, String password, String domain, String workstation) {
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.workstation = workstation;
        String localNtlmMsg1 = null;
        try {
            engine = new JCIFSEngine();
            localNtlmMsg1 = engine.generateType1Msg(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ntlmMsg1 = localNtlmMsg1;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        final List<String> WWWAuthenticate = response.headers().values("WWW-Authenticate");
        if (WWWAuthenticate.contains("NTLM")) {
            return response.request().newBuilder().header("Authorization", "NTLM " + ntlmMsg1).build();
        }
        String ntlmMsg3 = null;
        try {
            ntlmMsg3 = engine.generateType3Msg(username, password, domain, workstation, WWWAuthenticate.get(0).substring(5));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to set up JCIFS NTLM proxy authentication, Please check your NTLM parameters and provide the correct value of domain, workstation, username and password");
        }
        return response.request().newBuilder().header("Authorization", "NTLM " + ntlmMsg3).build();
    }
}
