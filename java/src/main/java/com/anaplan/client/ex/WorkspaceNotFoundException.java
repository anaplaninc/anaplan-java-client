package com.anaplan.client.ex;

/**
 * Created by Spondon Saha
 * Date: 4/18/18
 * Time: 8:37 PM
 */
public class WorkspaceNotFoundException extends RuntimeException {

    private static final String msg = "Workspace not found for Workspace-ID: ";

    public WorkspaceNotFoundException(String workspaceId) {
        super(msg + workspaceId);
    }

    public WorkspaceNotFoundException(String workspaceId, Throwable t) {
        super(msg + workspaceId, t);
    }
}
