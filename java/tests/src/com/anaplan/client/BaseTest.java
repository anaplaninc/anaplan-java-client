// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.Arrays;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;

public abstract class BaseTest extends TestCase {
    private Service service;
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
    
    public void setUp() {
        FileInputStream propertiesStream = null;
        try {
            propertiesStream = new FileInputStream("test.properties");
            ResourceBundle properties = new PropertyResourceBundle(propertiesStream);
            URI serviceLocation = new URI(properties.getString("service.location"));
            service = new Service(serviceLocation);
            service.setDebugLevel(2);
            correctCredentials = new Credentials(properties.getString("user.name"), properties.getString("user.password"));
            lowerCaseCredentials = new Credentials(properties.getString("user.name"), properties.getString("user.password").toLowerCase());
            upperCaseCredentials = new Credentials(properties.getString("user.name"), properties.getString("user.password").toUpperCase());
            incorrectCredentials = new Credentials(properties.getString("user.name"), properties.getString("user.password") + "#");
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

            service.setServiceCredentials(correctCredentials);
            if (properties.containsKey("proxy.location")) {
                String proxyLocation = properties.getString("proxy.location");
                if (null != proxyLocation && !proxyLocation.trim().isEmpty()) {
                    service.setProxyLocation(new URI(proxyLocation));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("setUp failed", e);
        } finally {
            if (null != propertiesStream) {
                try {
                    propertiesStream.close();
                } catch(IOException ioe) {
                }
            }
        }
    }

    public void tearDown() {
        if (null != service) {
            service.close();
        }
    }

    protected Service getService() {
        return service;
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
        return getService().getWorkspace(testWorkspace);
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
        InputStream inputStream = getClass().getResourceAsStream(name + ".gz");
        if (null == inputStream) {
            inputStream = getClass().getResourceAsStream(name);
            if (null == inputStream) {
                throw new FileNotFoundException(name);
            }
            return inputStream;
        }
        return new GZIPInputStream(inputStream);
    }

    protected File getTestDataFile(String name) throws IOException {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        File file = File.createTempFile("apitest", name.substring(name.lastIndexOf('.')));
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
                try {
                    outputStream.close();
                } catch (IOException ioException) {
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException ioException) {
                }
            }
        }
        file.setReadOnly();
        file.deleteOnExit();
        return file;
    }
        
    protected static void assertEquals(File expected, File actual) throws IOException {
        if (expected.length() != actual.length()) {
            fail("File sizes differ: " + expected + " and " + actual);
        }
        byte[] expectedContent = new byte[(int) expected.length()];
        byte[] actualContent = new byte[(int) actual.length()];
        RandomAccessFile expectedRAF = null;
        RandomAccessFile actualRAF = null;
        try {
            expectedRAF = new RandomAccessFile(expected, "r");
            expectedRAF.readFully(expectedContent);
        } finally {
            if (null != expectedRAF) {
                expectedRAF.close();
            }
        }
        try {
            actualRAF = new RandomAccessFile(actual, "r");
            actualRAF.readFully(actualContent);
        } finally {
            if (null != actualRAF) {
                actualRAF.close();
            }
        }
        if (!Arrays.equals(expectedContent, actualContent)) {
            fail("Content differs: " + expected + " and " + actual);
        }
    }    
    protected static TaskResult runTask(TaskFactory taskFactory, TaskParameters taskParameters) throws AnaplanAPIException {
        if (null == taskParameters) taskParameters = new TaskParameters();
        taskParameters.setLocale("en", "UK");
        Task task = taskFactory.createTask(taskParameters);
        TaskStatus taskStatus = null;
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
}
