package com.anaplan.client.ex;
/**
 * Throws error in case of issues with user directory
 */
public class BadSystemPropertyError extends RuntimeException {
    public BadSystemPropertyError (Throwable t) {
        super("Invalid System Property found" , t);
    }
}
