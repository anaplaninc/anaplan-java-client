package com.anaplan.client.ex;

public class ActionsNotFoundException extends RuntimeException {
    public ActionsNotFoundException(String modelId, Throwable t) {
        super("Actions not found for Model-ID=" + modelId, t);
    }
}
