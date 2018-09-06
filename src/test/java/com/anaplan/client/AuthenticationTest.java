// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.auth.MockFeignException;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.AnaplanAPITransportException;
import com.anaplan.client.ex.UserNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.when;


public class AuthenticationTest extends BaseTest {

    private Service mockService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationTest.class.getName());

    @Before
    public void setUp() throws AnaplanAPITransportException {
        mockService = getMockService();
    }

    @Test
    public void testGoodAuth() throws Exception {
        recordActionsFetchMockWorkspace();
        checkWorkspace();
    }

    @Test
    public void testBadAuth() throws IOException {
        recordActionsFetchMockWorkspace();
        mockService.getApiProvider().setApiClient(null);
        when(getMockAuthApi().authenticateBasic(okhttp3.Credentials.basic(
                getProps().getApiCredentials().getUserName(),
                getProps().getApiCredentials().getPassPhrase())))
                .thenThrow(new MockFeignException("Authentication failed!"));
        try {
            checkWorkspace();
            fail("Expected authentication to fail!");
        } catch (UserNotFoundException e) {
            logger.info("Expected Auth failure");
        }
    }

    public void checkWorkspace() throws AnaplanAPIException {
        Workspace workspace = mockService.getWorkspace(getTestWorkspaceNameOrId());
        assertEquals(getTestWorkspaceNameOrId(), workspace.getId());
        assertEquals("Workspace A", workspace.getName());
    }
}
