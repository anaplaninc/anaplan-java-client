// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.auth.Credentials;
import com.anaplan.client.dto.responses.ModelResponse;
import com.anaplan.client.dto.responses.UserResponse;
import com.anaplan.client.dto.responses.WorkspaceResponse;
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.AnaplanAPITransportException;
import com.anaplan.client.transport.ConnectionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;


public abstract class BaseTest {
    private static final String testWorkspaceNameOrId = "testWorkspaceNameOrId";
    private static final String testUserId = "testUserId";
    private Service mockService;
    private Credentials correctCredentials;
    private Credentials lowerCaseCredentials;
    private Credentials upperCaseCredentials;
    private Credentials incorrectCredentials;
    private String[] workspaceIds;
    private String[] workspaceNames;
    private String testWorkspace;
    private String archivedModel;
    private String lockedModel;
    private String testModel;
    private AnaplanAPI mockAnaplanApi;
    private AnaplanAuthenticationAPI mockAuthApi;
    private ObjectMapper objectMapper;
    private ConnectionProperties props;

    private static Map<String, byte[]> fixtures = new HashMap<>();
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
    protected static final String KEY_ALGORITHM = "RSA";

    public String getWorkspaceNameOrId() {
        return testWorkspaceNameOrId;
    }

    public String getTestUserId() {
        return testUserId;
    }

    public <T> T createFeignResponse(String fixturePath, Class<T> responseClass) throws IOException {
        return objectMapper.readValue(getFixture(fixturePath), responseClass);
    }

    /**
     * Lazily loads fixtures.
     *
     * @param fixtureName Name of the file to load.
     * @return
     * @throws IOException
     */
    protected byte[] getFixture(String fixtureName) throws IOException {
        InputStream fixtureStream;
        if (!fixtures.containsKey(fixtureName)) {
            if (fixtureName.contains("File_0v1.txt")
                    || fixtureName.contains("File_0v3.txt")) {
                fixtureStream = getTestDataStream(fixtureName);
            } else {
                fixtureStream = getClass().getClassLoader()
                        .getResourceAsStream(fixtureName);
            }
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
            lowerCaseCredentials = new Credentials(properties.getString("user.name"),
                    properties.getString("user.password").toLowerCase());
            upperCaseCredentials = new Credentials(properties.getString("user.name"),
                    properties.getString("user.password").toUpperCase());
            incorrectCredentials = new Credentials(properties.getString("user.name"),
                    properties.getString("user.password") + "#");

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

            mockService = Mockito.spy(new Service(props));
            objectMapper = mockService.getApiProvider().getObjectMapper();
            mockAnaplanApi = Mockito.mock(AnaplanAPI.class);
            mockAuthApi = Mockito.mock(AnaplanAuthenticationAPI.class);
            mockService.getApiProvider().setApiClient(mockAnaplanApi);
            mockService.getAuthProvider().setAuthClient(mockAuthApi);

            // collect data from properties
            workspaceIds = new String[2];
            workspaceIds[0] = properties.getString("workspace.0.id");
            workspaceIds[1] = properties.getString("workspace.1.id");
            workspaceNames = new String[2];
            workspaceNames[0] = properties.getString("workspace.0.name");
            workspaceNames[1] = properties.getString("workspace.1.name");
            testWorkspace = properties.getString("test.workspace");
            archivedModel = properties.getString("archived.model");
            lockedModel = properties.getString("locked.model");
            testModel = properties.getString("test.model");
            recordFetchUserId();
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

    public static String getTestWorkspaceNameOrId() {
        return testWorkspaceNameOrId;
    }

    public AnaplanAPI getMockAnaplanApi() {
        return mockAnaplanApi;
    }

    public AnaplanAuthenticationAPI getMockAuthApi() {
        return mockAuthApi;
    }

    public ConnectionProperties getProps() {
        return props;
    }

    /**
     * Getters
     */
    protected Service getMockService() {
        return mockService;
    }

    protected TemporaryFolder getTempFolder() {
        return tempFolder;
    }

    protected void recordFetchUserId() throws IOException {
        Mockito.doReturn(createFeignResponse("responses/users_me.json", UserResponse.class))
                .when(mockAnaplanApi)
                .getUser();
    }

    protected void recordActionsFetchMockWorkspace() throws IOException {
        Mockito.doReturn(createFeignResponse("responses/workspace_response.json", WorkspaceResponse.class))
                .when(mockAnaplanApi)
                .getWorkspace(testUserId, testWorkspaceNameOrId);
    }

    protected void recordActionsFetchMockModel() throws IOException {
        Mockito.doReturn(createFeignResponse("responses/model_response.json", ModelResponse.class))
                .when(mockAnaplanApi)
                .getModel(testUserId, "testModelGuid");
    }

    protected Model fetchMockModel() throws IOException, AnaplanAPIException {
        recordActionsFetchMockWorkspace();
        recordActionsFetchMockModel();
        return getTestModel();
    }

    protected Credentials getCorrectCredentials() {
        return correctCredentials;
    }

    protected Credentials getLowerCaseCredentials() {
        return lowerCaseCredentials;
    }

    protected Credentials getUpperCaseCredentials() {
        return upperCaseCredentials;
    }

    protected Credentials getIncorrectCredentials() {
        return incorrectCredentials;
    }

    protected String getWorkspaceId(int i) {
        return 0 <= i && i < workspaceIds.length ? workspaceIds[i] : null;
    }

    protected String getWorkspaceName(int i) {
        return 0 <= i && i < workspaceNames.length ? workspaceNames[i] : null;
    }

    protected Workspace getTestWorkspace() throws AnaplanAPIException {
        return mockService.getWorkspace(testWorkspace);
    }

    protected Model getArchivedModel() throws AnaplanAPIException {
        return getTestWorkspace().getModel(archivedModel);
    }

    protected Model getLockedModel() throws AnaplanAPIException {
        return getTestWorkspace().getModel(lockedModel);
    }

    protected Model getTestModel() throws AnaplanAPIException {
        return getTestWorkspace().getModel(testModel);
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

    /**
     * Creates "apitest.<extention>" file, usually "apitest.txt".
     *
     * @param name Name of the test data file to read
     * @return
     * @throws IOException
     */
    protected File getTestDataFile(String name) throws IOException {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        File file = tempFolder.newFile("apitest" + name.substring(name.lastIndexOf('.')));
        try {
            outputStream = new FileOutputStream(file);
            inputStream = getTestDataStream(name);
            byte[] buffer = new byte[8192];
            int read;
            do {
                read = inputStream.read(buffer);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                }
            } while (read != -1);
        } finally {
            if (null != outputStream) {
                outputStream.close();
            }
            if (null != inputStream) {
                inputStream.close();
            }
        }
        file.deleteOnExit();
        return file;
    }

    protected static void assertFilesEquals(File expected, File actual)
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        byte[] expectedContent = new byte[(int) expected.length()];
        byte[] actualContent = new byte[(int) actual.length()];
        RandomAccessFile expectedRAF = null;
        RandomAccessFile actualRAF = null;
        try {
            expectedRAF = new RandomAccessFile(expected, "r");
            expectedRAF.readFully(expectedContent);
            actualRAF = new RandomAccessFile(actual, "r");
            actualRAF.readFully(actualContent);
        } finally {
            if (null != actualRAF) {
                actualRAF.close();
            }
            if (null != expectedRAF) {
                expectedRAF.close();
            }
        }
        // check content equality
        if (!FileUtils.contentEquals(expected, actual)) {
            fail("Contents are not equal!");
        }

        // check SHA-512 hash of each file source
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        if (!MessageDigest.isEqual(md.digest(expectedContent), md.digest(actualContent))) {
            fail("Sha hash differs!");
        }
    }

    protected static TaskResult runTask(TaskFactory taskFactory, TaskParameters taskParameters) throws AnaplanAPIException {
        if (null == taskParameters) taskParameters = new TaskParameters();
        taskParameters.setLocale("en", "UK");
        Task task = taskFactory.createTask(taskParameters);
        TaskStatus taskStatus;
        do {
            try {
                Thread.sleep(250);
            } catch (InterruptedException interruptedException) {
            }
            taskStatus = task.getStatus();
        } while (!(taskStatus.getTaskState() == TaskStatus.State.COMPLETE
                || taskStatus.getTaskState() == TaskStatus.State.CANCELLED));
        assertEquals(TaskStatus.State.COMPLETE, taskStatus.getTaskState());
        return taskStatus.getResult();
    }

    protected RSAPrivateKey openPkcs8PKFile(String privateKeyFilePath) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try (FileInputStream fs = new FileInputStream(privateKeyFilePath)) {
            byte[] privateKeyContents = IOUtils.toByteArray(fs);
            return (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privateKeyContents));
        } catch (IOException e) {
            throw new AnaplanAPITransportException("Private-key file not found: ", e);
        }
    }
}
