// Copyright 2012 Anaplan Limited
package com.anaplan.client;

import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.dto.ServerFileData;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.dto.responses.ServerFileResponse;
import com.anaplan.client.dto.responses.ServerFilesResponse;
import com.anaplan.client.ex.AnaplanAPIException;
import com.google.common.io.Files;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


public class ServerFileTest extends BaseTest {

    private Model mockModel;
    private ServerFile mockServerFile;
    private static Logger logger = LoggerFactory.getLogger(ServerFileTest.class.getName());
    private static final String chunksResponseFile = "responses/file_chunks_response.json";
    private static final String fixtureFile1 = "files/File_0v1.txt";
    private static final String fixtureFile3 = "files/File_0v3.txt";
    private static final String createFileResponse = "responses/create_file_response.json";
    private static int chunkSize = 1048576;
    private AnaplanAPI anaplanAPI;

    @Before
    public void setUp() throws AnaplanAPIException, IOException {
        mockModel = fetchMockModel();
        anaplanAPI = mockModel.getApi();
        when(mockModel.getApi().getServerFiles(mockModel.getWorkspace().getId(), mockModel.getId(), 0))
                .thenReturn(createFeignResponse("responses/list_of_files_response.json", ServerFilesResponse.class));
        mockServerFile = mockModel.getServerFile("113000000025");
    }

    @Test
    public void testDownloadFileWithOverwrite() throws Exception {
        // mock out fetching of file chunks from server and
        when(mockModel.getApi().getChunks(
                mockModel.getWorkspace().getId(),
                mockModel.getId(),
                mockServerFile.getId()))
                .thenReturn(createFeignResponse(chunksResponseFile, ChunksResponse.class));
        when(mockModel.getApi().getChunkContent(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("test".getBytes());
        // try downloading file by overwrite=false
        File check0v1 = getTempFolder().newFile("apitest_temp.txt");
        try {
            mockServerFile.downLoad(check0v1, false);
            fail("Expected failure: file exists");
        } catch (Exception e) {
            logger.info("Could not download file as expected!");
            assertThat(e.getMessage(), CoreMatchers.containsString("already exists"));
        }

        // download file with overwrite=true
        when(mockModel.getApi().getChunkContent(mockModel.getWorkspace().getId(), mockModel.getId(),
                mockServerFile.getId(), "0"))
                .thenReturn("test".getBytes());
        mockServerFile.downLoad(check0v1, true);
        assertEquals("test", Files.asCharSource(check0v1, Charset.defaultCharset()).read());
    }

    @Test
    public void testFileUploadStream() throws Exception {

        InputStream source0v3 = null;
        OutputStream uploadStream = null;

        // mock out call to PUT the data to the server chunk by chunk, and POST
        // operation to close the transaction
        when(mockModel.getApi().upsertFileDataSource(anyString(), anyString(), anyString(), any(ServerFileData.class)))
                .thenReturn(new ServerFileResponse() {{
                    setItem(mockServerFile.getData());
                }});
        doNothing()
                .when(anaplanAPI)
                .uploadChunkCompressed(anyString(), anyString(), anyString(), anyString(), any(byte[].class));
        doReturn(new ServerFileResponse() {{
            setItem(mockServerFile.getData());
        }})
                .when(anaplanAPI)
                .completeUpload(mockModel.getWorkspace().getId(), mockModel.getId(), mockServerFile.getId(), mockServerFile.getData());
        try {
            source0v3 = getTestDataStream(fixtureFile3);
            uploadStream = mockServerFile.getUploadStream(chunkSize);
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
                uploadStream.close();
            }
            if (null != source0v3) {
                source0v3.close();
            }
        }
    }

    @Test
    public void testDownloadStream() throws Exception {
        InputStream downloadStream = null;
        File check0v3File = getTempFolder().newFile("download_data.txt");
        OutputStream check0v3Stream = null;

        // mock out calls to GET
        doReturn(createFeignResponse(chunksResponseFile, ChunksResponse.class))
                .when(anaplanAPI)
                .getChunks(mockModel.getWorkspace().getId(), mockModel.getId(), mockServerFile.getId());
        doReturn(getFixture(fixtureFile1))
                .when(anaplanAPI)
                .getChunkContent(mockModel.getWorkspace().getId(), mockModel.getId(), mockServerFile.getId(), "0");
        try {
            check0v3Stream = new FileOutputStream(check0v3File);
            downloadStream = mockServerFile.getDownloadStream();
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
                check0v3Stream.close();
            }
            if (null != downloadStream) {
                downloadStream.close();
            }
        }
        File file0v3 = getTestDataFile(fixtureFile1);
        assertFilesEquals(file0v3, check0v3File);
    }

    /**
     * testing last index Of comma separator
     * @throws Exception
     */
    @Test
    public void testlastIndexOfComma() throws Exception {
        int size =100;
        byte[] buffer = new byte[size];
        File sourceFile = new File("src/test/resources/files/indexOfCommaSeparator.txt");
        RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");
        raf.readFully(buffer, 0, size);
        int indexOfLastSeparator = mockServerFile.lastIndexOf(buffer,",");
        assertEquals(90,indexOfLastSeparator);
    }

    /**
     * testing last index Of tab separator
     * @throws Exception
     */
    @Test
    public void testlastIndexOftab() throws Exception {
        int size =100;
        byte[] buffer = new byte[size];
        File sourceFile = new File("src/test/resources/files/indexOfTabSeparator.txt");
        RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");
        raf.readFully(buffer, 0, size);
        int indexOfLastSeparator = mockServerFile.lastIndexOf(buffer,"\t");
        assertEquals(90,indexOfLastSeparator);
    }

    @Test
    public void testCreateServerFile() throws Exception {
        String newFileName = "NewFile1";
        // mock out calls to POST to server to help create new Server-File.
        doReturn(createFeignResponse(createFileResponse, ServerFileResponse.class))
                .when(anaplanAPI)
                .createImportDataSource(anyString(), anyString(), anyString(), any(ServerFileData.class));
        ServerFile serverFile1 = mockModel.createServerFileImportDataSource(
                newFileName, "TEST");
        assertNotNull(serverFile1.getData());
        assertEquals("113000000080", serverFile1.getData().getId());
        assertEquals(newFileName, serverFile1.getData().getName());
        assertEquals("\"", serverFile1.getData().getDelimiter());
        assertEquals("\t", serverFile1.getData().getSeparator());
        assertEquals("UTF-8", serverFile1.getData().getEncoding());
    }
}
