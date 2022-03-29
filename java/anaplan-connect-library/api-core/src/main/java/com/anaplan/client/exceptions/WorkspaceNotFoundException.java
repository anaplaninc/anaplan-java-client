package com.anaplan.client.exceptions;

/**
 * Created by Spondon Saha Date: 4/18/18 Time: 8:37 PM
 */
public class WorkspaceNotFoundException extends RuntimeException {

  private static final String MSG = "Workspace not found for Workspace-ID: ";

  public WorkspaceNotFoundException(String workspaceId) {
    super(MSG + workspaceId);
  }

  public WorkspaceNotFoundException(String workspaceId, Throwable t) {
    super(MSG + workspaceId, t);
  }
}
