package com.anaplan.client;

import com.anaplan.client.auth.AuthRetryTest;
import com.anaplan.client.auth.BasicAuthenticatorTest;
import com.anaplan.client.auth.CertificateAuthenticatorTest;
import com.anaplan.client.auth.KeyStoreManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/29/17
 * Time: 2:57 PM
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ActionTest.class,
        AuthenticationTest.class,
        AuthRetryTest.class,
        BasicAuthenticatorTest.class,
        CertificateAuthenticatorTest.class,
        JDBCCellReaderTest.class,
        KeyStoreManagerTest.class,
        ModelTest.class,
        ServerFileTest.class,
        WorkspaceTest.class
})
public class FunctionalTestSuite {
}
