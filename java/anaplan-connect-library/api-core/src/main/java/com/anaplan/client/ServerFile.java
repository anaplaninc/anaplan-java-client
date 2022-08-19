//   Copyright 2011, 2012 Anaplan Inc.
//
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.anaplan.client;

import com.anaplan.client.dto.ChunkData;
import com.anaplan.client.dto.ServerFileData;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.dto.responses.ServerFileResponse;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.exceptions.AnaplanChunkException;
import com.anaplan.client.exceptions.CreateImportDatasourceError;
import com.anaplan.client.exceptions.NoChunkError;
import com.anaplan.client.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a file on the Anaplan server associated with a model.
 * <p>
 * <p>
 * For importing data a file is first uploaded then its associated import is run. When exporting data an export process
 * is run, then the resulting server file is downloaded
 */
public class ServerFile extends NamedObject {

  private static final Logger LOG = LoggerFactory.getLogger(ServerFile.class);
  private ServerFileData data;

  protected static final char[] ONE_CHAR_SUPPORTED_SEPARATORS = new char[]{'\t', ',', ';'};
  public static final String ERROR_MULTIPLE_SEPPARATORS = "Error: Multiple column separators found in file ";
  private static final String PATH = "Path \"";
  private static final String FILE = "File \"";

  ServerFile(Model model, ServerFileData data) {
    super(model, data);
    this.data = data;
  }

  @Override
  public ServerFileData getData() {
    return data;
  }

  public void setData(ServerFileData data) {
    this.data = data;
  }

  /**
   * Gets the list of File chunks from the server
   *
   * @return {@link ChunkData}
   */
  public List<ChunkData> getChunks() {
    return getApi().getChunks(
        getWorkspace().getId(),
        getModel().getId(),
        getId())
        .getItem();
  }

  /**
   * Fetches the Chunk content as a byte array
   *
   * @param chunkId the chunk identifier
   * @return content in bytes
   */
  public byte[] getChunkContent(String chunkId) {
    return getApi().getChunkContent(
        getWorkspace().getId(),
        getModel().getId(),
        getId(),
        chunkId);
  }

  /**
   * Download a file from the server, writing it to the specified target file.
   *
   * @param target         The target file to write to
   * @param deleteExisting If true, the target file will automatically be deleted if it already exists; otherwise an
   *                       Exception will be thrown
   */
  public void downLoad(File target, boolean deleteExisting) throws IOException {
    if (target == null) {
      throw new FileNotFoundException("Target file does not exist.");
    }
    LogUtils.logSeparatorDownload();
    LOG.info("Downloading file {}", target.getAbsolutePath());
    Utils.checkTarget(target, PATH, FILE, deleteExisting);

    target = Utils.getAbsolutePath(target.toPath()).toFile();

    // We will write to a temporary location first and move it to its final
    // destination only when complete.
    File partial = new File(target.getParentFile(), ".partial."
        + target.getName());
    if(!partial.exists()){
      partial.getParentFile().mkdirs();
      if (!partial.createNewFile()) {
        LOG.warn("Warning: failed to create file {}", partial);
      }
    }
    RandomAccessFile partialFile = new RandomAccessFile(partial, "rw");
    try {
      partialFile.setLength(0);

      // Get list of chunks from server
      List<ChunkData> chunkList = getChunks();
      //checking in case chunklist is null
      if (chunkList != null) {
        for (ChunkData chunk : chunkList) {
          byte[] chunkContent = getChunkContent(chunk.getId());
          if (chunkContent == null) {
            throw new NoChunkError(chunk.getId());
          }
          partialFile.write(chunkContent);
        }
      }
      partialFile.close();
      partialFile = null;
      if (!partial.renameTo(target)) {
        LOG.warn("Warning: failed to rename file {} to {}", partial, target);
      }
    } finally {
      if (partialFile != null) {
        try {
          partialFile.close();
        } catch (IOException ioException) {
          LOG.warn("Warning: failed to close file {}: {}", partial, ioException.getMessage());
        }
      }
    }
  }

  /**
   * Create an InputStream implementation which will download the content from the server.
   *
   * @return an InputStream which will read the content stored on the server
   * @since 1.2
   */
  public InputStream getDownloadStream() {
    // Get list of chunks from server
    final List<ChunkData> chunkList = getApi().getChunks(getModel().getWorkspace().getId(),
        getModel().getId(), getId()).getItem();
    return new SequenceInputStream(new Enumeration<InputStream>() {
      int index = 0;

      @Override
      public boolean hasMoreElements() {
        int chunkListSize = chunkList == null ? 0 : chunkList.size();
        return index < chunkListSize;
      }

      @Override
      public InputStream nextElement() {
        try {
          byte[] chunkContent = getChunkContent(chunkList.get(index++).getId());
          return new ByteArrayInputStream(chunkContent);
        } catch (Exception thrown) {
          throw new AnaplanChunkException(
              "Failed to read chunk from server", thrown);
        }
      }
    });
  }

  /**
   * Create a {@link com.anaplan.client.CellReader} implementation which will download the content from the server. The
   * content is assumed to be in the same format as written to by getUploadCellWriter.
   *
   * @return a {@link com.anaplan.client.CellReader} which will read the content stored on the server
   * @since 1.2
   */
  public CellReader getDownloadCellReader() throws IOException {
    LogUtils.logSeparatorDownload();
    final LineNumberReader lnr = new LineNumberReader(
        new InputStreamReader(getDownloadStream(), StandardCharsets.UTF_8));
    String headerLine = lnr.readLine();
    final String[] headerRow = headerLine == null ? new String[0]
        : headerLine.split("\\t");

    return new CellReader() {
      @Override
      public String[] getHeaderRow() throws AnaplanAPIException {
        return headerRow;
      }

      @Override
      public String[] readDataRow() throws AnaplanAPIException,
          IOException {
        String dataLine = lnr.readLine();
        return dataLine == null ? null : dataLine.split("\\t");
      }

      /**
       * Nothing to do
       */
      @Override
      public void close() {
        //Nothing to do
      }
    };
  }

  /**
   * Upload a file to the server, writing it to the specified target file.
   *
   * @param source         The source file to upload
   * @param deleteExisting If true, the target server file will automatically be deleted if it already exists; otherwise
   *                       a FileException will be thrown
   */
  public void upLoad(File source, boolean deleteExisting, int chunkSize) throws IOException {
    LogUtils.logSeparatorUpload();
    LOG.info("Uploading file: {}", source.getAbsolutePath());
    Utils.isFileAndReadable(source.toPath());
    // split the separators string from the server to be able to count them, multiple separators are not supported atm
    List<String> separators = splitSeparators(data.getSeparator());
    if (separators.size() > 1) {
      throw new IllegalStateException(ERROR_MULTIPLE_SEPPARATORS + source.getName());
    }
    try (RandomAccessFile sourceFile = new RandomAccessFile(source, "r")) {
      long length = source.length();
      data.setChunkCount((int) ((length - 1) / chunkSize) + 1);
      ServerFileResponse response = getApi()
          .upsertFileDataSource(getWorkspace().getId(), getModel().getId(), getId(), data);
      if (response == null || response.getItem() == null) {
        throw new CreateImportDatasourceError(getName());
      }
      data = setServerFile(response, data);
      // Get list of chunks from server
      ChunksResponse chunks = getApi().getChunks(getWorkspace().getId(), getModel().getId(), getId());
      if (chunks == null || chunks.getItem() == null) {
        throw new CreateImportDatasourceError(getName());
      }
      List<ChunkData> chunkList = chunks.getItem();
      Iterator<ChunkData> chunkIterator = chunkList.iterator();
      byte[] buffer = new byte[chunkSize];

      long totalReadSoFar = 0;
      while (chunkIterator.hasNext()) {
        ChunkData chunk = chunkIterator.next();
        int size = chunkSize;
        if (!chunkIterator.hasNext()) {
          size = (int) (length - totalReadSoFar);
        }
        if (size != buffer.length) {
          buffer = new byte[size];
        }
        sourceFile.readFully(buffer, 0, size);
        //reading the last index of the separator
        int separatorLastIndex = lastIndexOf(buffer, data.getSeparator());
        //determining the byte offset based on UTF-16LE encoding
        int offset = data.getEncoding().equalsIgnoreCase("UTF-16LE") ? 2 : 1;
        //calculating the size of byte array to load the bytes until the last index of separator
        int finalSize = separatorLastIndex < 0 ? buffer.length : separatorLastIndex + offset;
        //creating the buffer to load the byte array until last separator
        byte[] finalBuffer = new byte[finalSize];
        //copying the data from existing byte array to new byte array until last separator
        System.arraycopy(buffer, 0, finalBuffer, 0, finalSize);
        //calculating the total read size from the file
        totalReadSoFar += finalSize;
        //checking if there is another chunk to decide if to upload the newly created buffer or existing buffer.
        //existing buffer will be uploaded in case of last chunk
        uploadChunk(chunkIterator, finalBuffer, buffer, chunk.getId());
        sourceFile.seek(totalReadSoFar);
        LOG.debug("Uploaded chunk: {} (size={}MB)", chunk.getId(), chunkSize / 1000000);
      }
    }
  }

  private void uploadChunk(final Iterator<ChunkData> chunkIterator, final byte[] finalBuffer, byte[] buffer, String chunkId){
    if (chunkIterator.hasNext()) {
      getApi()
          .uploadChunkCompressed(getWorkspace().getId(), getModel().getId(), getId(), chunkId, finalBuffer);
    } else {
      getApi().uploadChunkCompressed(getWorkspace().getId(), getModel().getId(), getId(), chunkId, buffer);
    }
  }

  private ServerFileData setServerFile(final ServerFileResponse response, final ServerFileData data) {
    ServerFileData dataResponse = response.getItem();
    if (data.getHeaderRow() != null && data.getHeaderRow() == -1) {
      dataResponse.setHeaderRow(1);
    }
    if (data.getFirstDataRow() != null && data.getFirstDataRow() == -1) {
      dataResponse.setFirstDataRow(2);
    }
    return dataResponse;
  }

  /*
   * Split the separators from the concatenated server side string
   * First we try to identify the default one char separators then we consider the remaining string (if not empty)
   * sa a custom separator
   */
  public static List<String> splitSeparators(String separators) {
    if (separators == null || separators.length() < 1) {
      throw new IllegalStateException("Data separator cannot be empty");
    }
    List<String> separatorList = new ArrayList<>();
    for (char c : ONE_CHAR_SUPPORTED_SEPARATORS) {
      if (separators.charAt(0) == c) {
        separatorList.add(String.valueOf(c));
        if (separators.length() > 1) {
          separators = separators.substring(1);
        } else {
          separators = "";
          break;
        }
      }
    }
    if (separators.length() > 0) {
      separatorList.add(separators);//can be multi character
    }
    return separatorList;
  }

  /**
   * returns the last index of single byte separator from a byte array
   *
   * @param outerArray bytes array to be check
   * @param separator the separator
   * @return last index of a single byte separator
   */
  public int lastIndexOf(byte[] outerArray, String separator) {
    byte[] smallerArray = separator.getBytes();
    for (int i = outerArray.length - smallerArray.length; i > 0; --i) {
      boolean found = true;
      for (int j = 0; j < smallerArray.length; ++j) {
        if (outerArray[i + j] != smallerArray[j]) {
          found = false;
          break;
        }
      }
      if (found) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Finalizes the upload-stream and updates it's metadata.
   */
  private void finalizeUploadStream() {
    try {
      ServerFileResponse completeResponse = getApi().completeUpload(
          getWorkspace().getId(),
          getModel().getId(),
          getId(),
          data);
      if (completeResponse == null || completeResponse.getItem() == null) {
        throw new CreateImportDatasourceError(getId());
      }
      data = completeResponse.getItem();
    } catch (Exception e) {
      throw new AnaplanAPIException("Failed to finalize file-upload: " + getId(), e);
    }
  }

  /**
   * Return a new OutputStream implementation which will upload written content to the server, writing it to the
   * specified target file. Any existing data will be replaced. The server file will be considered incomplete until the
   * close() method is invoked on the OutputStream
   *
   * @return The OutputStream to write to
   * @since 1.2
   */
  public OutputStream getUploadStream(final int chunkSize) {
    data.setChunkCount(-1);
    ServerFileResponse response = getApi()
        .upsertFileDataSource(getWorkspace().getId(), getModel().getId(), getId(), data);
    if (response == null || response.getItem() == null) {
      throw new CreateImportDatasourceError(data.getName());
    }
    data = response.getItem();
    return new FilterOutputStream(new ByteArrayOutputStream(chunkSize * 2)) {
      int chunkIndex = 0;

      private ByteArrayOutputStream getBuffer() {
        return (ByteArrayOutputStream) out;
      }

      @Override
      public void write(int b) throws IOException {
        super.write(b);
        if (getBuffer().size() >= chunkSize) {
          flush();
        }
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        if (getBuffer().size() >= chunkSize) {
          flush();
        }
      }

      @Override
      public void flush() throws IOException {
        super.flush();
        try {
          getApi().uploadChunkCompressed(
              getWorkspace().getId(),
              getModel().getId(),
              getId(),
              String.valueOf(chunkIndex++),
              getBuffer().toByteArray());
          LOG.debug("Uploaded chunk: {} (size={}MB)", chunkIndex, chunkSize / 1000000);
        } catch (Exception e) {
          throw new AnaplanAPIException("Failed to upload chunk(" + chunkIndex + "): " + getId(), e);
        }
        getBuffer().reset();
      }

      @Override
      public void close() throws IOException {
        flush();
        finalizeUploadStream();
      }
    };
  }

  /**
   * Return a {@link com.anaplan.client.CellWriter} implementation which will upload written content to the server,
   * writing it to the specified target file. The file will have the following format:
   * <ul>
   * <li>Encoding: UTF-8</li>
   * <li>Column separator: tab character</li>
   * <li>Delimiter: none - the presence of a tab character in a cell will
   * cause an exception to be thrown</li>
   * <li>Header row number: 1</li>
   * <li>First data row: 2</li>
   * </ul>
   *
   * @return a {@link com.anaplan.client.CellWriter} implementation
   * @since 1.2
   */
  public CellWriter getUploadCellWriter(final int chunkSize) {
    return new CellWriter() {
      OutputStream output;

      @Override
      public void writeHeaderRow(Object[] row)
          throws AnaplanAPIException, IOException {
        output = getUploadStream(chunkSize);
        writeDataRow(row);
      }

      @Override
      public void writeDataRow(Object[] row) throws AnaplanAPIException,
          IOException {
        if (output == null) {
          throw new AnaplanAPIException(
              "Cell writer is no longer open");
        }
        String separator = getSeparator();
        StringBuilder buf = new StringBuilder();
        int col = 0;
        for (Object item : row) {
          if (col++ != 0) {
            buf.append(separator);
          }
          String text = item.toString();
          if (text.contains(separator)) {
            throw new AnaplanAPIException(
                "Cell text cannot contain separator " + separator);
          }
          buf.append(text);
        }
        output.write(buf.append('\n').toString().getBytes(StandardCharsets.UTF_8));
      }

      @Override
      public int writeDataRow(final DataRow dataRow)
          throws AnaplanAPIException {
        //dummy value as the implementation is done in JdbcCellWriter
        return 1;
      }

      @Override
      public void close() throws IOException {
        if (output == null) {
          return;
        }
        createServerFile(data);
        output.close();
        output = null;
      }

      @Override
      public void abort() {
        output = null;
      }
    };
  }

  private void createServerFile(final ServerFileData data) {
    data.setFormat(data.getFormat() == null ? "txt" : data.getFormat());
    data.setEncoding(data.getEncoding() == null ? StandardCharsets.UTF_8.toString() : data.getEncoding());
    data.setSeparator(getSeparator());
    data.setDelimiter(data.getDelimiter() == null ? "\"" : data.getDelimiter());
    data.setHeaderRow(data.getHeaderRow() == -1 ? 1 : data.getHeaderRow());
    data.setFirstDataRow(data.getFirstDataRow() == -1 ? 2 : data.getFirstDataRow());
  }
  /*
   * Retrieve the current separator: use the one from the server file data if present, or else use "\t" as default
   *
   * @return The separator as string
   */
  private String getSeparator() {
    return Optional.ofNullable(getData())
        .map(ServerFileData::getSeparator)
        .orElse("\t");
  }

}
