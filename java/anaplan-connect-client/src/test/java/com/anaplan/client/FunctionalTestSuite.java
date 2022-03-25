package com.anaplan.client;

import com.anaplan.client.auth.AuthRetryTest;
import com.anaplan.client.auth.BasicAuthenticatorTest;
import com.anaplan.client.auth.CertificateAuthenticatorTest;
import com.anaplan.client.auth.KeyStoreManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Spondon Saha User: spondonsaha Date: 6/29/17 Time: 2:57 PM
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AuthRetryTest.class,
    BasicAuthenticatorTest.class,
    CertificateAuthenticatorTest.class,
    KeyStoreManagerTest.class
})
public class FunctionalTestSuite {

}
