//   Copyright 2011 Anaplan Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.anaplan.client.auth;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

/**
 * Credentials for accessing the Anaplan API service.
 */
public final class Credentials {
    /**
     * Represents an authentication method.
     */
    public enum Scheme {
        BASIC,
        NTLM,
        CA_CERTIFICATE
    }

    private Scheme scheme;

    private String userName;
    private String passPhrase;
    private String domain;
    private String workstation;

    private X509Certificate certificate;
    private RSAPrivateKey privateKey;

    /**
     * Create user/passPhrase credentials for service authentication, or
     * simple/digest proxy authentication.
     *
     * @param userName   The user name (e-mail address) for the authenticating user
     * @param passPhrase The passPhrase for the authenticating user
     */
    public Credentials(String userName, String passPhrase) {
        this.userName = userName;
        this.passPhrase = passPhrase;
        this.domain = workstation = null;
        this.certificate = null;
        this.scheme = Scheme.BASIC;
    }

    /**
     * Create user/passPhrase credentials for NTLM proxy authentication.
     * This form is only used when accessing the service through a proxy using
     * the NTLM scheme.
     *
     * @param userName    The user name (e-mail address) for the authenticating user
     * @param passPhrase  The passPhrase for the authenticating user
     * @param domain      The domain to be authenticated against
     * @param workstation The name of the workstation
     * @since 1.3.1
     */
    public Credentials(
            String userName,
            String passPhrase,
            String domain,
            String workstation
    ) {
        this.userName = userName;
        this.passPhrase = passPhrase;
        this.domain = domain;
        this.workstation = workstation;
        this.certificate = null;
        this.scheme = Scheme.NTLM;
    }

    /**
     * Create credentials for service authentication using user certificates.
     *
     * @param certificate the certificate
     * @param privateKey  Private key
     */
    public Credentials(X509Certificate certificate, RSAPrivateKey privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.userName = passPhrase = domain = workstation = null;
        this.scheme = Scheme.CA_CERTIFICATE;
    }

    /**
     * Get the authentication method.
     */
    public Scheme getScheme() {
        return scheme;
    }

    /**
     * Get the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Get the passPhrase.
     */
    public String getPassPhrase() {
        return passPhrase;
    }

    /**
     * Get the domain. This is only used when accessing the service through a
     * proxy using the NTLM scheme.
     *
     * @since 1.3.1
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Get the workstation name. This is only used when accessing the service
     * through a proxy using the NTLM scheme.
     *
     * @since 1.3.1
     */
    public String getWorkstation() {
        return workstation;
    }

    /**
     * Get the certificate
     *
     * @since 1.3.2
     */
    public X509Certificate getCertificate() {
        return certificate;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public boolean isNtlm() {
        return domain != null || workstation != null;
    }
}
