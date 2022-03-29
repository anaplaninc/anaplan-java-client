// Copyright 2012 Anaplan Limited
package com.anaplan.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.anaplan.client.AnaplanApiProviderImpl;
import com.anaplan.client.Model;
import com.anaplan.client.ObjectMapperProvider;
import com.anaplan.client.Service;
import com.anaplan.client.Task;
import com.anaplan.client.TaskFactory;
import com.anaplan.client.TaskParameters;
import com.anaplan.client.TaskResult;
import com.anaplan.client.TaskStatus;
import com.anaplan.client.Workspace;
import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.auth.Credentials;
import com.anaplan.client.dto.responses.ModelResponse;
import com.anaplan.client.dto.responses.WorkspaceResponse;
import com.anaplan.client.dto.responses.WorkspacesResponse;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.transport.ConnectionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


public abstract class BaseTest {

  private static final String testWorkspaceNameOrId = "testWorkspaceNameOrId";
  private static final Map<String, byte[]> fixtures = new HashMap<>();
  private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
  public Service mockService;
  @TempDir
  public Path tempFolderPath;
  private Credentials correctCredentials;
  private String[] workspaceIds;
  private String[] workspaceNames;
  private String testWorkspace;
  private String testworkspacebyname;
  private String testModel;
  private AnaplanAPI mockAnaplanApi;
  private AnaplanAuthenticationAPI mockAuthApi;
  private ObjectMapper objectMapper;
  private ConnectionProperties props;

  public static String getTestWorkspaceNameOrId() {
    return testWorkspaceNameOrId;
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

  protected static TaskResult runTask(TaskFactory taskFactory, TaskParameters taskParameters)
      throws AnaplanAPIException {
    if (null == taskParameters) {
      taskParameters = new TaskParameters();
    }
    taskParameters.setLocale("en", "UK");
    Task task = taskFactory.createTask(taskParameters);
    TaskStatus taskStatus;
    do {
      taskStatus = task.getStatus();
    } while (!(taskStatus.getTaskState() == TaskStatus.State.COMPLETE
        || taskStatus.getTaskState() == TaskStatus.State.CANCELLED));
    assertEquals(TaskStatus.State.COMPLETE, taskStatus.getTaskState());
    return taskStatus.getResult();
  }


  public <T> T createFeignResponse(String fixturePath, Class<T> responseClass) throws IOException {
    return objectMapper.readValue(getFixture(fixturePath), responseClass);
  }

  /**
   * Lazily loads fixtures.
   *
   * @param fixtureName Name of the file to load.
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

  @BeforeEach
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
      AnaplanApiProviderImpl anaplanApiProvider = new AnaplanApiProviderImpl(props,null,null);
      objectMapper = ObjectMapperProvider.getObjectMapper();
      mockAuthApi = Mockito.mock(AnaplanAuthenticationAPI.class);

      // collect data from properties
      workspaceIds = new String[2];
      workspaceIds[0] = properties.getString("workspace.0.id");
      workspaceIds[1] = properties.getString("workspace.1.id");
      workspaceNames = new String[2];
      workspaceNames[0] = properties.getString("workspace.0.name");
      workspaceNames[1] = properties.getString("workspace.1.name");
      testWorkspace = properties.getString("test.workspace");
      testworkspacebyname = properties.getString("test.workspacebyName");
      testModel = properties.getString("test.model");
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

  @AfterEach
  public void tearDown() {
    if (null != mockService) {
      mockService.close();
    }
    Mockito.reset(mockAnaplanApi);
    Mockito.reset(mockService);
  }

  /**
   * Getters
   */
  protected Service getMockService() {
    return mockService;
  }

  protected Path getTempFolderPath() {
    return tempFolderPath;
  }

  protected void recordActionsFetchMockWorkspace() throws IOException {
    Mockito
        .doReturn(createFeignResponse("responses/workspace_response.json", WorkspaceResponse.class))
        .when(mockAnaplanApi)
        .getWorkspace(testWorkspaceNameOrId);
  }

  protected void recordActionsFetchMockWorkspaces() throws IOException {
    Mockito.doReturn(
        createFeignResponse("responses/list_of_workspaces_response.json", WorkspacesResponse.class))
        .when(mockAnaplanApi)
        .getWorkspaces(0);
  }

  protected void recordActionsFetchMockModel() throws IOException {
    Mockito.doReturn(createFeignResponse("responses/model_response.json", ModelResponse.class))
        .when(mockAnaplanApi)
        .getModel("testModelGuid");
  }

  protected Model fetchMockModel() throws IOException, AnaplanAPIException {
    recordActionsFetchMockWorkspace();
    recordActionsFetchMockWorkspaces();
    recordActionsFetchMockModel();
    return getTestModel();
  }

  protected Workspace getTestWorkspacebyId() throws AnaplanAPIException {
    return mockService.getWorkspace(testWorkspace);
  }

  protected Workspace getTestWorkspacebyName() throws AnaplanAPIException {
    return mockService.getWorkspace(testworkspacebyname);
  }

  protected Model getTestModel() throws AnaplanAPIException {
    return getTestWorkspacebyId().getModel(testModel);
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
   */
  protected File getTestDataFile(String name) throws IOException {
    OutputStream outputStream = null;
    InputStream inputStream = null;
    Path path = Files.createFile(tempFolderPath.resolve("apitest" + name.substring(name.lastIndexOf('.'))));
    File file = new File(path.toString());
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

}
