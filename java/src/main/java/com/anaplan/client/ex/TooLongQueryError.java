package com.anaplan.client.ex;

/**
 * Thrown whenever the provided JDBC Sql query is longer than 65535 characters.
 */
public class TooLongQueryError extends RuntimeException {
    public TooLongQueryError(int queryLength) {
        super("Too long query length (" + queryLength + " characters). Cannot be larger than 65535 characters!");
    }
}
