package com.anaplan.client.ex;

public class ExportsNotFoundException extends RuntimeException {
    public ExportsNotFoundException(String modelId, Throwable t) {
        super("Exports not found for Model-ID=" + modelId, t);
    }
}
