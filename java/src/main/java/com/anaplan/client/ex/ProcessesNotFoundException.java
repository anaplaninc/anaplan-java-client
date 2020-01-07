package com.anaplan.client.ex;

public class ProcessesNotFoundException extends RuntimeException {
    public ProcessesNotFoundException(String modelId, Throwable t) {
        super("Processes not found for Model-ID=" + modelId, t);
    }
}
