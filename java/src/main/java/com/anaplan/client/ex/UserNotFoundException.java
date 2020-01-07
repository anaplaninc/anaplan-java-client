package com.anaplan.client.ex;

/**
 * Thrown when user does not exist in the system
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Throwable t) {
        super("User not recognized!", t);
    }
}