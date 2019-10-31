package com.anaplan.client.ex;

public class ModulesNotFoundException extends RuntimeException {
    public ModulesNotFoundException(String modelId, Throwable t) {
        super("Modules not found for Model-ID=" + modelId, t);
    }
}
