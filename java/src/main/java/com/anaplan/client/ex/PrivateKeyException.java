package com.anaplan.client.ex;

/**
 * Throws an exception when there is an issue while loading the privatekey
 */
public class PrivateKeyException extends RuntimeException {
    public PrivateKeyException(String privateKeyPath) {
        super("Could not load your privateKey :" + privateKeyPath + ". There might be an issue with your privateKey or passphrase.");
    }
}
