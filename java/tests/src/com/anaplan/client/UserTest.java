// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import java.util.List;

public class UserTest extends BaseTest {
    public void testUser() throws AnaplanAPIException {
        checkWorkspaces();
        getService().setServiceCredentials(getLowerCaseCredentials());
        checkWorkspaces();
        getService().setServiceCredentials(getUpperCaseCredentials());
        checkWorkspaces();
        getService().setServiceCredentials(getIncorrectCredentials());
        try {
            getService().getWorkspaces();
            fail("Expected authentication failure");
        } catch (AnaplanAPIException apiException) {
        }
        try {
            getService().getWorkspace(getWorkspaceId(0));
            fail("Expected authentication failure");
        } catch (AnaplanAPIException apiException) {
        }
        getService().setServiceCredentials(getCorrectCredentials());
        Workspace workspace0 = getService().getWorkspace(getWorkspaceId(0));
        assertEquals(getWorkspaceId(0), workspace0.getId());
        assertEquals(getWorkspaceName(0), workspace0.getName());
        Workspace workspace1 = getService().getWorkspace(getWorkspaceName(1));
        assertEquals(getWorkspaceId(1), workspace1.getId());
        assertEquals(getWorkspaceName(1), workspace1.getName());
    }
    public void checkWorkspaces() throws AnaplanAPIException {
        List<Workspace> workspaces = getService().getWorkspaces();
        assertEquals(2, workspaces.size());
        Workspace workspace0 = workspaces.get(0);
        assertEquals(getWorkspaceId(0), workspace0.getId());
        assertEquals(getWorkspaceName(0), workspace0.getName());
        Workspace workspace1 = workspaces.get(1);
        assertEquals(getWorkspaceId(1), workspace1.getId());
        assertEquals(getWorkspaceName(1), workspace1.getName());
    }
}
