//   Copyright 2011, 2012 Anaplan Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
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
import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.ex.CreateImportDatasourceError;
import com.anaplan.client.ex.NoChunkError;
import com.anaplan.client.logging.LogUtils;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a file on the Anaplan server associated with a model.
 * <p>
 * <p>
 * For importing data a file is first uploaded then its associated import is
 * run. When exporting data an export process is run, then the resulting server
 * file is downloaded
 */
public class ServerFile extends NamedObject {

    private static final Logger LOG = LoggerFactory.getLogger(ServerFile.class);
    private ServerFileData data;

    ServerFile(Model model, ServerFileData data) {
        super(model, data);
        this.data = data;
    }

    public ServerFileData getData() {
        return this.data;
    }

    public void setData(ServerFileData data) {
        this.data = data;
    }

    /**
     * Gets the list of File chunks from the server
     *
     * @return
     */
    List<ChunkData> getChunks() {
        return getApi().getChunks(
                getWorkspace().getId(),
                getModel().getId(),
                getId())
                .getItem();
    }

    /**
     * Fetches the Chunk content as a byte array
     *
     * @param chunkId
     * @return
     */
    byte[] getChunkContent(String chunkId) {
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
     * @param deleteExisting If true, the target file will automatically be deleted if it
     *                       already exists; otherwise an Exception will be thrown
     */
    public void downLoad(File target, boolean deleteExisting) throws IOException {
        LogUtils.logSeparatorDownload();
        LOG.info("Downloading file {}", target.getAbsolutePath());
        if (target.exists()) {
            if (!target.isFile()) {
                throw new FileNotFoundException("Path \"" + target
                                                        + "\" exists but is not a file");
            } else if (!deleteExisting) {
                throw new IllegalStateException("File \"" + target
                                                        + "\" already exists");
            } else if (!target.canWrite()) {
                throw new FileNotFoundException(
                        "File \""
                                + target
                                + "\" cannot be written to - check ownership and/or permissions");
            }
            target.delete();
        }
        // We will write to a temporary location first and move it to its final
        // destination only when complete.
        File partial = new File(target.getParentFile(), ".partial."
                + target.getName());
        RandomAccessFile partialFile = new RandomAccessFile(partial, "rw");
        try {
            partialFile.setLength(0);

            // Get list of chunks from server
            List<ChunkData> chunkList = getChunks();
            for (ChunkData chunk : chunkList) {
                byte[] chunkContent = getChunkContent(chunk.getId());
                if (chunkContent == null) {
                    throw new NoChunkError(chunk.getId());
                }
                partialFile.write(chunkContent);
            }
            partialFile.close();
            partialFile = null;
            partial.renameTo(target);
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
     * Create an InputStream implementation which will download the content from
     * the server.
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
                return this.index < chunkListSize;
            }

            @Override
            public InputStream nextElement() {
                try {
                    byte[] chunkContent = getChunkContent(chunkList.get(this.index++).getId());
                    return new ByteArrayInputStream(chunkContent);
                } catch (Exception thrown) {
                    throw new RuntimeException(
                            "Failed to read chunk from server", thrown);
                }
            }
        });
    }

    /**
     * Create a {@link com.anaplan.client.CellReader} implementation which will
     * download the content from the server. The content is assumed to be in the
     * same format as written to by getUploadCellWriter.
     *
     * @return a {@link com.anaplan.client.CellReader} which will read the
     * content stored on the server
     * @since 1.2
     */
    public CellReader getDownloadCellReader() throws IOException {
        LogUtils.logSeparatorDownload();
        final LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(getDownloadStream(), "UTF-8"));
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

            @Override
            public void close() {
            }
        };
    }

    /**
     * Upload a file to the server, writing it to the specified target file.
     *
     * @param source         The source file to upload
     * @param deleteExisting If true, the target server file will automatically be deleted
     *                       if it already exists; otherwise a FileException will be thrown
     */
    public void upLoad(File source, boolean deleteExisting, int chunkSize) throws IOException {
        LogUtils.logSeparatorUpload();
        LOG.info("Uploading file: {}", source.getAbsolutePath());
        if (!source.exists()) {
            throw new FileNotFoundException("Path \"" + source
                                                    + "\" does not exist");
        }
        if (!source.isFile()) {
            throw new FileNotFoundException("Path \"" + source
                                                    + "\" does not refer to a file");
        }
        if (!source.canRead()) {
            throw new FileNotFoundException("File \"" + source
                                                    + "\" cannot be read - check ownership and/or permissions");
        }
        RandomAccessFile sourceFile = null;
        try {
            long length = source.length();
            this.data.setChunkCount((int) ((length - 1) / chunkSize) + 1);
            ServerFileResponse response = getApi().upsertFileDataSource(getWorkspace().getId(), getModel().getId(), getId(), this.data);
            System.setProperty("file.encoding", this.data.getEncoding());
            if (response == null || response.getItem() == null) {
                throw new CreateImportDatasourceError(getName());
            }
            this.data = response.getItem();
            this.data.setHeaderRow(this.data.getHeaderRow() == -1 ? 1 : this.data.getHeaderRow());
            this.data.setFirstDataRow(this.data.getFirstDataRow() == -1 ? 2 : this.data.getFirstDataRow());
            // Get list of chunks from server
            ChunksResponse chunks = getApi().getChunks(getWorkspace().getId(), getModel().getId(), getId());
            if (chunks == null || chunks.getItem() == null) {
                throw new CreateImportDatasourceError(getName());
            }
            List<ChunkData> chunkList = chunks.getItem();
            Iterator<ChunkData> chunkIterator = chunkList.iterator();
            byte[] buffer = new byte[chunkSize];
            sourceFile = new RandomAccessFile(source, "r");
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
                int separatorLastIndex = lastIndexOf(buffer, "\n");
                //calculating the size of byte array to load the bytes until the last index of separator
                //creating the buffer to load the byte array until last separator
                byte[] finalBuffer = new byte[size];
                //copying the data from existing byte array to new byte array until last separator
                System.arraycopy(buffer, 0, finalBuffer, 0, size);
                //calculating the total read size from the file
                totalReadSoFar += size;
                //checking if there is another chunk to decide if to upload the newly created buffer or existing buffer.
                //existing buffer will be uploaded in case of last chunk
                if (chunkIterator.hasNext()) {
                    getApi().uploadChunkCompressed(getWorkspace().getId(), getModel().getId(), getId(), chunk.getId(), finalBuffer);
                } else {
                    getApi().uploadChunkCompressed(getWorkspace().getId(), getModel().getId(), getId(), chunk.getId(), buffer);
                }
                sourceFile.seek(totalReadSoFar);
                LOG.debug("Uploaded chunk: {} (size={}MB)", chunk.getId(), chunkSize / 1000000);
            }
        } finally {
            if (sourceFile != null) {
                try {
                    sourceFile.close();
                } catch (IOException ioException) {
                    LOG.warn("Warning: failed to close file {}: {}", source, ioException.getMessage());
                }
            }
        }
    }

    /**
     * returns the last index of single byte separator from a byte array
     *
     * @param outerArray
     * @param separator
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
                    this.data);
            if (completeResponse == null || completeResponse.getItem() == null) {
                throw new CreateImportDatasourceError(getId());
            }
            this.data = completeResponse.getItem();
        } catch (FeignException e) {
            throw new AnaplanAPIException("Failed to finalize file-upload: " + getId(), e);
        }
    }

    /**
     * Return a new OutputStream implementation which will upload written
     * content to the server, writing it to the specified target file. Any
     * existing data will be replaced. The server file will be considered
     * incomplete until the close() method is invoked on the OutputStream
     *
     * @return The OutputStream to write to
     * @since 1.2
     */
    public OutputStream getUploadStream(final int chunkSize) {
        this.data.setChunkCount(-1);
        ServerFileResponse response = getApi().upsertFileDataSource(getWorkspace().getId(), getModel().getId(), getId(), this.data);
        if (response == null || response.getItem() == null) {
            throw new CreateImportDatasourceError(this.data.getName());
        }
        this.data = response.getItem();
        return new FilterOutputStream(new ByteArrayOutputStream(chunkSize * 2)) {
            int chunkIndex = 0;

            private ByteArrayOutputStream getBuffer() {
                return (ByteArrayOutputStream) this.out;
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
                            String.valueOf(this.chunkIndex++),
                            getBuffer().toByteArray());
                    LOG.debug("Uploaded chunk: {} (size={}MB)", this.chunkIndex, chunkSize / 1000000);
                } catch (FeignException e) {
                    throw new AnaplanAPIException("Failed to upload chunk(" + this.chunkIndex + "): " + getId(), e);
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
     * Return a {@link com.anaplan.client.CellWriter} implementation which will
     * upload written content to the server, writing it to the specified target
     * file. The file will have the following format:
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
                this.output = getUploadStream(chunkSize);
                writeDataRow(row);
            }

            @Override
            public void writeDataRow(Object[] row) throws AnaplanAPIException,
                    IOException {
                if (this.output == null) {
                    throw new AnaplanAPIException(
                            "Cell writer is no longer open");
                }
                StringBuilder buf = new StringBuilder();
                int col = 0;
                for (Object item : row) {
                    if (col++ != 0) {
                        buf.append('\t');
                    }
                    String text = item.toString();
                    if (text.indexOf('\t') != -1) {
                        final String tabChar = "\t";
                        text = text.replaceAll(tabChar, " ");
                    }
                    buf.append(text);
                }
                this.output.write(buf.append('\n').toString().getBytes("UTF-8"));
            }

            @Override
            public int writeDataRow(String exportId, int maxRetryCount, int retryTimeout, InputStream inputStream, int noOfChunks, String chunkId, int[] mapcols, int columnCount, String separator) throws AnaplanAPIException, IOException, SQLException {
                //dummy value as the implementation is done in JdbcCellWriter
                return 1;
            }

            @Override
            public void close() throws IOException {
                if (this.output != null) {
                    ServerFile.this.data.setFormat(ServerFile.this.data.getFormat() == null ? "txt" : ServerFile.this.data.getFormat());
                    ServerFile.this.data.setEncoding(ServerFile.this.data.getEncoding() == null ? StandardCharsets.UTF_8.toString() : ServerFile.this.data.getEncoding());
                    ServerFile.this.data.setSeparator("\t");
                    ServerFile.this.data.setDelimiter(ServerFile.this.data.getDelimiter() == null ? "\"" : ServerFile.this.data.getDelimiter());
                    ServerFile.this.data.setHeaderRow(ServerFile.this.data.getHeaderRow() == -1 ? 1 : ServerFile.this.data.getHeaderRow());
                    ServerFile.this.data.setFirstDataRow(ServerFile.this.data.getFirstDataRow() == -1 ? 2 : ServerFile.this.data.getFirstDataRow());
                    this.output.close();
                    this.output = null;
                }
            }

            @Override
            public void abort() {
                this.output = null;
            }
        };
    }
}
