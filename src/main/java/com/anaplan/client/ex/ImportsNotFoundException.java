package com.anaplan.client.ex;

public class ImportsNotFoundException extends RuntimeException {
    public ImportsNotFoundException(String modelId, Throwable t) {
        super("Imports not found for Model-ID=" + modelId, t);
    }
}
