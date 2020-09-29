// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.AnaplanAPITransportException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertEquals;


public class AuthenticationTest extends BaseTest {

    private Service mockService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationTest.class.getName());

    @Before
    public void setUp() throws AnaplanAPITransportException {
        mockService = getMockService();
    }

    @Test
    public void testGoodAuth() throws Exception {
        checkWorkspace();
    }

    public void checkWorkspace() throws AnaplanAPIException {
        Workspace workspace = getTestWorkspace();
        assertEquals(getTestWorkspaceNameOrId(), workspace.getId());
    }
}
