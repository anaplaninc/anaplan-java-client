// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.auth.Credentials;
import com.anaplan.client.exceptions.AnaplanAPITransportException;
import com.anaplan.client.transport.ConnectionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


public abstract class BaseTest {

  protected static final String KEY_ALGORITHM = "RSA";
  private static final Map<String, byte[]> fixtures = new HashMap<>();
  private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
  public Service mockService;
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  private Credentials correctCredentials;
  private String[] workspaceIds;
  private String[] workspaceNames;
  private AnaplanAPI mockAnaplanApi;
  private AnaplanAuthenticationAPI mockAuthApi;
  private ObjectMapper objectMapper;
  private ConnectionProperties props;

  /**
   * Lazily loads fixtures.
   *
   * @param fixtureName Name of the file to load.
   */
  protected byte[] getFixture(String fixtureName) throws IOException {
    InputStream fixtureStream;
    if (!fixtures.containsKey(fixtureName)) {
      fixtureStream = getClass().getClassLoader()
            .getResourceAsStream(fixtureName);
      fixtures.put(fixtureName, IOUtils.toByteArray(fixtureStream));
    }
    return fixtures.get(fixtureName);
  }

  @Before
  public void setUpBase() {
    MockitoAnnotations.initMocks(this);
    FileInputStream propertiesStream = null;
    try {
      propertiesStream = new FileInputStream("src/test/resources/test.properties");
      ResourceBundle properties = new PropertyResourceBundle(propertiesStream);
      URI serviceLocation = new URI(properties.getString("service.location"));

      correctCredentials = new Credentials(properties.getString("user.name"),
          properties.getString("user.password"));
      props = new ConnectionProperties();
      props.setAuthServiceUri(new URI(mockAuthServiceUrl));
      props.setApiServicesUri(serviceLocation);
      props.setApiCredentials(correctCredentials);

      if (properties.containsKey("proxy.location")) {
        String proxyLocation = properties.getString("proxy.location");
        if (!proxyLocation.trim().isEmpty()) {
          props.setProxyLocation(new URI(proxyLocation));
        }
      }

      mockAnaplanApi = Mockito.mock(AnaplanAPI.class);
      Supplier<AnaplanAPI> apiProvider = () -> mockAnaplanApi;
      mockService = Mockito.spy(new Service(props, null, apiProvider));
      objectMapper = ObjectMapperProvider.getObjectMapper();
      mockAuthApi = Mockito.mock(AnaplanAuthenticationAPI.class);

      // collect data from properties
      workspaceIds = new String[2];
      workspaceIds[0] = properties.getString("workspace.0.id");
      workspaceIds[1] = properties.getString("workspace.1.id");
      workspaceNames = new String[2];
      workspaceNames[0] = properties.getString("workspace.0.name");
      workspaceNames[1] = properties.getString("workspace.1.name");
    } catch (Exception e) {
      throw new RuntimeException("setUp failed", e);
    } finally {
      if (null != propertiesStream) {
        try {
          propertiesStream.close();
        } catch (IOException ioe) {
        }
      }
    }
  }

  @After
  public void tearDown() {
    if (null != mockService) {
      mockService.close();
    }
    Mockito.reset(mockAnaplanApi);
    Mockito.reset(mockService);
  }


  protected InputStream getTestDataStream(String name) throws IOException {
    InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream(name + ".gz");
    if (null == inputStream) {
      inputStream = getClass().getClassLoader().getResourceAsStream(name);
      if (null == inputStream) {
        throw new FileNotFoundException(name);
      }
      return inputStream;
    }
    return new GZIPInputStream(inputStream);
  }


  protected RSAPrivateKey openPkcs8PKFile(String privateKeyFilePath)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    try (FileInputStream fs = new FileInputStream(privateKeyFilePath)) {
      byte[] privateKeyContents = IOUtils.toByteArray(fs);
      return (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM)
          .generatePrivate(new PKCS8EncodedKeySpec(privateKeyContents));
    } catch (IOException e) {
      throw new AnaplanAPITransportException("Private-key file not found: ", e);
    }
  }
}
