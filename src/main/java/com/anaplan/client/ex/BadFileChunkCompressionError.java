package com.anaplan.client.ex;

/**
 * Created by Spondon Saha
 * Date: 4/23/18
 * Time: 11:57 AM
 */
public class BadFileChunkCompressionError extends RuntimeException {
    public BadFileChunkCompressionError(Throwable t) {
        super("Unable to compress file-chunk", t);
    }
}
