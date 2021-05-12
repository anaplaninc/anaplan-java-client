// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import static junit.framework.TestCase.assertEquals;

import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.exceptions.AnaplanAPITransportException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AuthenticationTest extends BaseTest {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationTest.class.getName());
  private Service mockService;

  @Before
  public void setUp() throws AnaplanAPITransportException {
    mockService = getMockService();
  }

  @Test
  public void testGoodAuth() throws Exception {
    recordActionsFetchMockWorkspaces();
    checkWorkspace();
  }

  public void checkWorkspace() throws AnaplanAPIException {
    Workspace workspace = mockService.getWorkspace(getTestWorkspaceNameOrId());
    assertEquals(getTestWorkspaceNameOrId(), workspace.getId());
    assertEquals("Workspace A", workspace.getName());
  }
}
