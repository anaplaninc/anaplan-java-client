// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class WorkspaceTest extends BaseTest {

  private static final String listOfModelsFixture = "responses/list_of_models_response.json";

  @Before
  public void setUp() throws Exception {
    recordActionsFetchMockWorkspaces();
  }

  @Test
  public void testWorkspace() throws Exception {
    Workspace testWorkspace = getTestWorkspacebyId();
    assertEquals("testWorkspaceNameOrId", testWorkspace.getId());
    assertEquals("Workspace A", testWorkspace.getName());
    testWorkspace = getTestWorkspacebyName();
    assertEquals("testWorkspaceNameOrId", testWorkspace.getId());
    assertEquals("Workspace A", testWorkspace.getName());
  }
}
