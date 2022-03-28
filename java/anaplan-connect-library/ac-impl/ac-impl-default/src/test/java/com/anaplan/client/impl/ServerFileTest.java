// Copyright 2012 Anaplan Limited
package com.anaplan.client.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.anaplan.client.Model;
import com.anaplan.client.ServerFile;
import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.dto.ServerFileData;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.dto.responses.ServerFileResponse;
import com.anaplan.client.dto.responses.ServerFilesResponse;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServerFileTest extends BaseTest {

  private static final String chunksResponseFile = "responses/file_chunks_response.json";
  private static final String multiChunksResponseFile = "responses/file_multi_chunks_response.json";
  private static final String fixtureFile1 = "files/File_0v1.txt";
  private static final String fixtureFile3 = "files/File_0v3.txt";
  private static final String createFileResponse = "responses/create_file_response.json";
  private static final Logger logger = LoggerFactory.getLogger(ServerFileTest.class.getName());
  private static final int chunkSize = 1048576;
  private Model mockModel;
  private ServerFile mockServerFile;
  private AnaplanAPI anaplanAPI;

  @BeforeEach
  public void setUp() throws AnaplanAPIException, IOException {
    mockModel = fetchMockModel();
    anaplanAPI = mockModel.getApi();
    when(mockModel.getApi().getServerFiles(mockModel.getWorkspace().getId(), mockModel.getId(), 0))
        .thenReturn(createFeignResponse("responses/list_of_files_response.json",
            ServerFilesResponse.class));
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
    when(mockModel.getApi().getChunkContent(
        Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
        .thenReturn("test".getBytes());
    // try downloading file by overwrite=false
    Path pathname = getTempFolderPath().resolve("apitest_temp.txt");
    java.nio.file.Files.createFile(pathname);
    File check0v1 = new File(pathname.toString());
    try {
      mockServerFile.downLoad(check0v1, false);
      fail("Expected failure: file exists");
    } catch (Throwable e) {
      logger.info("Could not download file as expected!");
      assertTrue(e.getMessage().contains("already exists"));
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
    when(mockModel.getApi()
        .upsertFileDataSource(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers
            .any(ServerFileData.class)))
        .thenReturn(new ServerFileResponse() {{
          setItem(mockServerFile.getData());
        }});
    doNothing()
        .when(anaplanAPI)
        .uploadChunkCompressed(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers
                .anyString(),
            Matchers.any(byte[].class));
    doReturn(new ServerFileResponse() {{
      setItem(mockServerFile.getData());
    }})
        .when(anaplanAPI)
        .completeUpload(mockModel.getWorkspace().getId(), mockModel.getId(), mockServerFile.getId(),
            mockServerFile.getData());
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
    Path pathname = getTempFolderPath().resolve("download_data.txt");
    java.nio.file.Files.createFile(pathname);
    File check0v3File = new File(pathname.toString());
    OutputStream check0v3Stream = null;
    // mock out calls to GET
    doReturn(createFeignResponse(chunksResponseFile, ChunksResponse.class))
        .when(anaplanAPI)
        .getChunks(mockModel.getWorkspace().getId(), mockModel.getId(), mockServerFile.getId());
    doReturn(getFixture(fixtureFile1))
        .when(anaplanAPI)
        .getChunkContent(mockModel.getWorkspace().getId(), mockModel.getId(),
            mockServerFile.getId(), "0");
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
   *
   * @throws Exception
   */
  @Test
  public void testlastIndexOfComma() throws Exception {
    int size = 100;
    byte[] buffer = new byte[size];
    File sourceFile = new File("src/test/resources/files/indexOfCommaSeparator.txt");
    RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");
    raf.readFully(buffer, 0, size);
    int indexOfLastSeparator = mockServerFile.lastIndexOf(buffer, ",");
    assertEquals(90, indexOfLastSeparator);
  }


  /**
   * testing last index Of no separator in file
   *
   * @throws Exception
   */
  @Test
  public void testlastIndexOfTabSeparatorNotPresentInData() throws Exception {
    int size = 100;
    byte[] buffer = new byte[size];
    File sourceFile = new File("src/test/resources/files/indexOfNoSeparator.txt");
    RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");
    raf.readFully(buffer, 0, size);
    int indexOfLastSeparator = mockServerFile.lastIndexOf(buffer, "\t");
    assertEquals(-1, indexOfLastSeparator);
  }

  /**
   * testing last index Of tab separator
   *
   * @throws Exception
   */
  @Test
  public void testlastIndexOftab() throws Exception {
    int size = 100;
    byte[] buffer = new byte[size];
    File sourceFile = new File("src/test/resources/files/indexOfTabSeparator.txt");
    RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");
    raf.readFully(buffer, 0, size);
    int indexOfLastSeparator = mockServerFile.lastIndexOf(buffer, "\t");
    assertEquals(90, indexOfLastSeparator);
  }

  @Test
  public void testFileUploadWithNoSeparator() throws IOException {
    String path = Objects
        .requireNonNull(this.getClass().getClassLoader().getResource("files/M1-orig.csv")).getPath();
    File file = new File(path);
    byte[]  fileContentBytes = java.nio.file.Files.readAllBytes(Paths.get(path));
    when(mockModel.getApi().getChunks(
        mockModel.getWorkspace().getId(),
        mockModel.getId(),
        mockServerFile.getId()))
        .thenReturn(createFeignResponse(multiChunksResponseFile, ChunksResponse.class));
    when(mockModel.getApi().getChunkContent(
        Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
        .thenReturn(Arrays.copyOfRange(fileContentBytes, 0, 2500))
        .thenReturn(Arrays.copyOfRange(fileContentBytes, 2500, fileContentBytes.length));
    when(mockModel.getApi()
        .upsertFileDataSource(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers
            .any(ServerFileData.class)))
        .thenReturn(new ServerFileResponse() {{
          setItem(mockServerFile.getData());
        }});
    ServerFileData sfd = new ServerFileData();
    sfd.setChunkCount(2);
    sfd.setSeparator("\t");
    sfd.setDelimiter(",");
    sfd.setCountry("UK");
    sfd.setEncoding("UTF-8");
    sfd.setFormat(".txt");
    sfd.setId("1130000000001");
    mockServerFile.setData(sfd);
    mockServerFile.upLoad(file, false, fetchChunkSize(1));
    Mockito.verify(mockModel.getApi(),
        new Times(1))
        .getChunks(mockModel.getWorkspace().getId(), mockModel.getId(), mockServerFile.getId());
    Mockito.verify(mockModel.getApi(),
        new Times(1))
        .upsertFileDataSource(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any(ServerFileData.class));
  }

  @Test
  public void testFileUploadFailsWithMultiSeparator() {
    File file = new File(this.getClass().getClassLoader().getResource("files/M1-orig.csv").getPath());
    ServerFileData sfd = new ServerFileData();
    sfd.setChunkCount(1);
    sfd.setSeparator(",;");
    sfd.setDelimiter(",");
    sfd.setCountry("UK");
    sfd.setEncoding("UTF-8");
    sfd.setFormat(".txt");
    sfd.setId("1130000000001");
    mockServerFile.setData(sfd);
    assertThrows(
        IllegalStateException.class, () -> mockServerFile.upLoad(file, false, fetchChunkSize(1)));
  }

  @Test
  public void testCreateServerFile() throws Exception {
    String newFileName = "NewFile1";
    // mock out calls to POST to server to help create new Server-File.
    doReturn(createFeignResponse(createFileResponse, ServerFileResponse.class))
        .when(anaplanAPI)
        .createImportDataSource(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers
            .any(ServerFileData.class));
    ServerFile serverFile1 = mockModel.createServerFileImportDataSource(
        newFileName, "TEST");
    assertNotNull(serverFile1.getData());
    assertEquals("113000000080", serverFile1.getData().getId());
    assertEquals(newFileName, serverFile1.getData().getName());
    assertEquals("\"", serverFile1.getData().getDelimiter());
    assertEquals("\t", serverFile1.getData().getSeparator());
    assertEquals("UTF-8", serverFile1.getData().getEncoding());
  }

  @Test
  public void splitSeparatorsEmpty() {
    assertThrows(IllegalStateException.class, () -> ServerFile.splitSeparators(""));
  }

  @Test
  public void splitSeparatorsNull() {
    assertThrows(IllegalStateException.class, ()->ServerFile.splitSeparators(null));
  }

  @Test
  public void splitSeparatorsOneChar() {
    assertEquals(Collections.singletonList("\t"), ServerFile.splitSeparators("\t"));
  }

  @Test
  public void splitSeparatorsMultiple() {
    assertEquals(Collections.singletonList("aaa"), ServerFile.splitSeparators("aaa"));
  }

  @Test
  public void splitSeparatorsCombination() {
    assertEquals(Arrays.asList(",", ";", "aaa"), ServerFile.splitSeparators(",;aaa"));
  }

  @Test
  public void splitSeparatorsAll() {
    assertEquals(Arrays.asList("\t", ",", ";", "aaa"), ServerFile.splitSeparators("\t,;aaa"));
  }

  @Test
  public void splitSeparatorsOrder() {
    assertEquals(Arrays.asList("aaa\t,;"), ServerFile.splitSeparators("aaa\t,;"));
  }

  static int fetchChunkSize(int value) {
    return  50 * 50 * value;
  }

}
