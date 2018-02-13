// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class FileTest extends BaseTest {
    public void testServerFile() throws AnaplanAPIException, IOException {
        testModel(getTestModel());
        try {
            testModel(getArchivedModel());
            fail("Expected failure: archived model");
        } catch (AnaplanAPIException apiException) {
        }
        getService().setServiceCredentials(getIncorrectCredentials());
        try {
            testModel(getTestModel());
            fail("Expected failure: authentication");
        } catch (AnaplanAPIException apiException) {
        }
    }
    public void testModel(Model model) throws AnaplanAPIException, IOException {
        ServerFile serverFile0 = model.getServerFile("File 0");
        File source0v1 = getTestDataFile("File_0v1.txt");
        serverFile0.upLoad(source0v1, true);
        File check0v1 = File.createTempFile("apitest", ".txt");
        try {
            serverFile0.downLoad(check0v1, false);
            fail("Expected failure: file exists");
        } catch (Exception e) {
        }
        serverFile0.downLoad(check0v1, true);
        assertEquals(source0v1, check0v1);
        check0v1.delete();
        File source0v2 = getTestDataFile("File_0v2.txt");
        serverFile0.upLoad(source0v2, true);
        File check0v2 = File.createTempFile("apitest", ".txt");
        serverFile0.downLoad(check0v2, true);
        assertEquals(source0v2, check0v2);
        check0v2.delete();
        InputStream source0v3 = null;
        OutputStream uploadStream = null;
        try {
            source0v3 = getTestDataStream("File_0v3.txt");
            uploadStream = serverFile0.getUploadStream();
            byte[] buffer = new byte[8192];
            int read;
            do {
                read = source0v3.read(buffer);
                if (read > 0) {
                    uploadStream.write(buffer, 0, read);
                }
            } while (read != -1);
        } finally {
            if (null != uploadStream) {
                try {
                    uploadStream.close();
                } catch (IOException ioException) {
                }
            }
            if (null != source0v3) {
                try {
                    source0v3.close();
                } catch (IOException ioException) {
                }
            }
        }
        InputStream downloadStream = null;
        File check0v3File = File.createTempFile("apitest", ".txt");
        OutputStream check0v3Stream = null;
        try {
            check0v3Stream = new FileOutputStream(check0v3File);
            downloadStream = serverFile0.getDownloadStream();
            byte[] buffer = new byte[8192];
            int read;
            do {
                read = downloadStream.read(buffer);
                if (read > 0) {
                    check0v3Stream.write(buffer, 0, read);
                }
            } while (read != -1);
        } finally {
            if (null != check0v3Stream) {
                try {
                    check0v3Stream.close();
                } catch (IOException ioException) {
                }
            }
            if (null != downloadStream) {
                try {
                    downloadStream.close();
                } catch (IOException ioException) {
                }
            }
        }
        File file0v3 = getTestDataFile("File_0v3.txt");
        assertEquals(file0v3, check0v3File);
        check0v3File.delete();

        try {
            ServerFile serverFileFail = model.createServerFileImportDataSource("List 0", "TEST");
            serverFileFail.setSeparator(",");
            serverFileFail.setDelimiter("\"");
            serverFileFail.setHeaderRow(1);
            serverFileFail.setFirstDataRow(2);
            serverFileFail.upLoad(getTestDataFile("File_0v1.txt"), true);
            fail("Expected failure: illegal model change");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ServerFile serverFile1 = model.createServerFileImportDataSource("File 1", "TEST");
        serverFile1.setSeparator(",");
        serverFile1.setDelimiter("\"");
        serverFile1.setHeaderRow(1);
        serverFile1.setFirstDataRow(2);
        serverFile1.upLoad(getTestDataFile("File_0v1.txt"), true);
        //System.out.write(getService().getSerializationHandler().serialize(serverFile1.data, ServerFile.DATA_TYPE));
    }
}
