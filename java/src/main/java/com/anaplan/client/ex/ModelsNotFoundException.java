package com.anaplan.client.ex;

public class ModelsNotFoundException extends RuntimeException {
    public ModelsNotFoundException(String workspaceId, Throwable t) {
        super("Models not found for Workspace-ID=" + workspaceId, t);
    }
}
