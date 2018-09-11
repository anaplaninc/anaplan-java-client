package com.anaplan.client.ex;

public class ServerFilesNotFoundException extends RuntimeException {
    public ServerFilesNotFoundException(String modelId, Throwable t) {
        super("Server-Files not found for Model-ID=" + modelId, t);
    }
}
