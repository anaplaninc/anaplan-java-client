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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.anaplan.client.serialization.TypeWrapper;

/**
 * Represents a file on the Anaplan server associated with a model.
 * 
 * <p>
 * For importing data a file is first uploaded then its associated import is
 * run. When exporting data an export process is run, then the resulting server
 * file is downloaded
 */
public class ServerFile extends NamedObject {

    private static final Logger logger = Logger.getLogger("anaplan-connect.file");

    /**
     * File formats recognised by Ananplan import.
     * 
     * @since 1.2
     */
    public static enum Format {
        /** Tabulated text */
        TXT
    };

    static class Data extends NamedObject.Data {
        int chunkCount;
        String origin;
        String format;
        String language;
        String country;
        String encoding;
        String separator;
        String delimiter;
        Integer headerRow;
        Integer firstDataRow;
    }

    static class Chunk extends NamedObject.Data {
    }

    static final TypeWrapper<Data> DATA_TYPE = new TypeWrapper<Data>() {
    };
    static final TypeWrapper<List<Data>> DATA_LIST_TYPE = new TypeWrapper<List<Data>>() {
    };
    static final TypeWrapper<List<Chunk>> CHUNK_LIST_TYPE = new TypeWrapper<List<Chunk>>() {
    };

    private Data data;

    ServerFile(Model model, Data data) {
        super(model, data);
        this.data = data;
    }

    String getPath() {
        return getModel().getPath() + "/files/" + getId();
    }

    /**
     * Download a file from the server, writing it to the specified target file.
     * 
     * @param target
     *            The target file to write to
     * @param deleteExisting
     *            If true, the target file will automatically be deleted if it
     *            already exists; otherwise an Exception will be thrown
     */
    public void downLoad(File target, boolean deleteExisting)
            throws AnaplanAPIException, IOException {
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
            byte[] content = getTransportProvider().get(getPath() + "/chunks",
                    getSerializationHandler().getContentType());
            List<Chunk> chunkList = getSerializationHandler().deserialize(
                    content, CHUNK_LIST_TYPE);
            for (Chunk chunk : chunkList) {
                byte[] chunkContent = getTransportProvider().get(
                        getPath() + "/chunks/" + chunk.id, null);
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
                    logger.warning("Warning: failed to close file "
                            + partial + ": " + ioException.getMessage());
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
    public InputStream getDownloadStream() throws AnaplanAPIException,
            IOException {
        // Get list of chunks from server
        byte[] content = getTransportProvider().get(getPath() + "/chunks",
                getSerializationHandler().getContentType());
        final List<Chunk> chunkList = getSerializationHandler().deserialize(
                content, CHUNK_LIST_TYPE);
        return new SequenceInputStream(new Enumeration<InputStream>() {
            int index = 0;

            public boolean hasMoreElements() {
                return index < chunkList.size();
            }

            public InputStream nextElement() {
                try {
                    byte[] chunkContent = getTransportProvider().get(
                            getPath() + "/chunks/" + chunkList.get(index++).id,
                            null);
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
     *         content stored on the server
     * @since 1.2
     */
    public CellReader getDownloadCellReader() throws AnaplanAPIException,
            IOException {
        final LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(getDownloadStream(), "UTF-8"));
        String headerLine = lnr.readLine();
        final String[] headerRow = headerLine == null ? new String[0]
                : headerLine.split("\\t");

        return new CellReader() {
            @Override
            public String[] getHeaderRow() throws AnaplanAPIException,
                    IOException {
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
     * @param source
     *            The source file to upload
     * @param deleteExisting
     *            If true, the target server file will automatically be deleted
     *            if it already exists; otherwise a FileException will be thrown
     */
    public void upLoad(File source, boolean deleteExisting)
            throws AnaplanAPIException, IOException {
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
            final int chunkSize = 1048576;
            data.chunkCount = (int) ((length - 1) / chunkSize) + 1;
            byte[] content = getSerializationHandler().serialize(data,
                    DATA_TYPE);
            String contentType = getSerializationHandler().getContentType();
            content = getTransportProvider().post(getPath(), content,
                    contentType, contentType);
            if (content != null) {
                data = getSerializationHandler()
                        .deserialize(content, DATA_TYPE);
            }
            // Get list of chunks from server
            content = getTransportProvider().get(getPath() + "/chunks",
                    contentType);
            List<Chunk> chunkList = getSerializationHandler().deserialize(
                    content, CHUNK_LIST_TYPE);
            Iterator<Chunk> chunkIterator = chunkList.iterator();
            byte[] buffer = new byte[chunkSize];
            sourceFile = new RandomAccessFile(source, "r");
            long totalReadSoFar = 0;
            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                int size = chunkSize;
                if (!chunkIterator.hasNext()) {
                    size = (int) (length - totalReadSoFar);
                }
                if (size != buffer.length) {
                    buffer = new byte[size];
                }
                sourceFile.readFully(buffer, 0, size);
                totalReadSoFar += size;
                getTransportProvider().put(getPath() + "/chunks/" + chunk.id,
                        buffer, null);
            }
        } finally {
            if (sourceFile != null) {
                try {
                    sourceFile.close();
                } catch (IOException ioException) {
                    logger.warning("Warning: failed to close file "
                            + source + ": " + ioException.getMessage());
                }
            }
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
    public OutputStream getUploadStream() throws AnaplanAPIException,
            IOException {
        final int chunkSize = 1048576;
        data.chunkCount = -1;
        byte[] content = getSerializationHandler().serialize(data, DATA_TYPE);
        String contentType = getSerializationHandler().getContentType();
        content = getTransportProvider().post(getPath(), content, contentType,
                contentType);
        if (content != null) {
            data = getSerializationHandler().deserialize(content, DATA_TYPE);
        }
        return new FilterOutputStream(new ByteArrayOutputStream(chunkSize * 2)) {
            int chunkIndex = 0;

            private ByteArrayOutputStream getBuffer() {
                return (ByteArrayOutputStream) out;
            }

            public void write(int b) throws IOException {
                super.write(b);
                if (getBuffer().size() >= chunkSize) {
                    flush();
                }
            }

            public void write(byte[] b, int off, int len) throws IOException {
                super.write(b, off, len);
                if (getBuffer().size() >= chunkSize) {
                    flush();
                }
            }

            public void flush() throws IOException {
                super.flush();
                try {
                    getTransportProvider().put(
                            getPath() + "/chunks/" + chunkIndex++,
                            getBuffer().toByteArray(), null);
                } catch (AnaplanAPIException apiException) {
                    throw new IOException(apiException);
                }
                getBuffer().reset();
            }

            public void close() throws IOException {
                flush();
                try {
                    byte[] content = getSerializationHandler().serialize(data,
                            DATA_TYPE);
                    String contentType = getSerializationHandler()
                            .getContentType();
                    content = getTransportProvider().post(
                            getPath() + "/complete", content, contentType,
                            contentType);
                    if (content != null) {
                        data = getSerializationHandler().deserialize(content,
                                DATA_TYPE);
                    }
                } catch (AnaplanAPIException apiException) {
                    throw new IOException(apiException);
                }
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
    public CellWriter getUploadCellWriter() throws AnaplanAPIException {
        data.format = "txt";
        data.encoding = "UTF-8";
        data.separator = "\t";
        data.delimiter = "";
        data.headerRow = 1;
        data.firstDataRow = 2;
        return new CellWriter() {
            OutputStream output;

            @Override
            public void writeHeaderRow(Object[] row)
                    throws AnaplanAPIException, IOException {
                output = getUploadStream();
                writeDataRow(row);
            }

            @Override
            public void writeDataRow(Object[] row) throws AnaplanAPIException,
                    IOException {
                if (output == null) {
                    throw new AnaplanAPIException(
                            "Cell writer is no longer open");
                }
                StringBuilder buf = new StringBuilder();
                for (Object item : row) {
                    if (buf.length() != 0)
                        buf.append("\t");
                    String text = item.toString();
                    if (text.indexOf('\t') != -1) {
                        throw new AnaplanAPIException(
                                "Cell text cannot contain tab character");
                    }
                    buf.append(text);
                }
                output.write(buf.append('\n').toString().getBytes("UTF-8"));
            }

            @Override
            public void close() throws IOException {
                if (output != null) {
                    output.close();
                    output = null;
                }
            }

            @Override
            public void abort() {
                output = null;
            }
        };
    }

    /**
     * Set the format of the file. If null or unset, the format will be
     * autodetected based on content.
     * 
     * @param format
     *            the file format
     * @since 1.2
     */
    public void setFormat(Format format) {
        data.format = (format == null ? null : String.valueOf(format));
    }

    /**
     * Get the format of the file. If autodetected, this will populated by the
     * server on completion of upload.
     * 
     * @return the file format
     * @since 1.2
     */
    public Format getFormat() {
        return data.format == null ? null : Format.valueOf(data.format);
    }

    /**
     * Set the language of the locale used for file autodetection.
     * 
     * @param language
     *            The two-letter ISO-639 code of the language.
     * @since 1.2
     */
    public void setLanguage(String language) {
        data.language = language;
    }

    /**
     * Set the country of the locale used for file autodetection.
     * 
     * @param country
     *            The two-letter ISO-3166 code of the country.
     * @since 1.2
     */
    public void setCountry(String country) {
        data.country = country;
    }

    /**
     * Set the character encoding used for the file. If null or unset, the
     * encoding will be autodetected based on content.
     * 
     * @param encoding
     *            The preferred MIME name of the encoding, or null. See <a
     *            href="http://www.iana.org/assignments/character-sets"
	 *            >http://www.iana.org/assignments/character-sets</a> for a
	 *            complete list.
	 * @since 1.2
	 */
	public void setEncoding(String encoding) {
		data.encoding = encoding;
	}

	/**
	 * Set the separator character(s) used to separate columns. If null or unset
	 * a separator will be autodetected based on content.
	 * 
	 * @param separator
	 *            A string containing 1+ separator characters, or null.
	 * @since 1.2
	 */
	public void setSeparator(String separator) {
		data.separator = separator;
	}

	/**
	 * Set the delimiter character used to enclose column cell data. If null or
	 * unset a delimiter will be autodetected based on content. The empty string
	 * is used to explicitly denote no delimiter
	 * 
	 * @param delimiter
	 *            A string containing a single delimiter character, the empty
	 *            string, or null.
	 * @since 1.2
	 */
	public void setDelimiter(String delimiter) {
		data.delimiter = delimiter;
	}

	/**
	 * Set the row number of the header row. If null or unset a header row will
	 * be autodetected based on content.
	 * 
	 * @param headerRow
	 *            Row number beginning at 1, zero if no header row present, or
	 *            null.
	 * @since 1.2
	 */
	public void setHeaderRow(Integer headerRow) {
		data.headerRow = headerRow;
	}

	/**
	 * Set the row number of the first data row. If null or unset the first data
	 * row will be autodetected based on content.
	 * 
	 * @param firstDataRow
	 *            Row number beginning at 1, or null.
	 * @since 1.2
	 */
	public void setFirstDataRow(Integer firstDataRow) {
		data.firstDataRow = firstDataRow;
	}

	/**
	 * Get the language of the locale used for file autodetection.
	 * 
	 * @return language The two-letter ISO-639 code of the language.
	 * @since 1.2
	 */
	public String getLanguage() {
		return data.language;
	}

	/**
	 * Get the country of the locale used for file autodetection.
	 * 
	 * @return country The two-letter ISO-3166 code of the country.
	 * @since 1.2
	 */
	public String getCountry() {
		return data.country;
	}

	/**
	 * Get the character encoding used for the file.
	 * 
	 * @return encoding The preferred MIME name of the encoding, or null. See <a
	 *         href="http://www.iana.org/assignments/character-sets">http://www.
     *         iana.org/assignments/character-sets</a> for a complete list.
     * @since 1.2
     */
    public String getEncoding() {
        return data.encoding;
    }

    /**
     * Get the separator character(s) used to separate columns.
     * 
     * @return separator A string containing 1+ separator characters, or null.
     * @since 1.2
     */
    public String getSeparator() {
        return data.separator;
    }

    /**
     * Get the delimiter character used to enclose column cell data. The empty
     * string is used to explicitly denote no delimiter
     * 
     * @return delimiter A string containing a single delimiter character, the
     *         empty string, or null.
     * @since 1.2
     */
    public String getDelimiter() {
        return data.delimiter;
    }

    /**
     * Get the row number of the header row.
     * 
     * @return headerRow Row number beginning at 1, zero if no header row
     *         present, or null.
     * @since 1.2
     */
    public Integer getHeaderRow() {
        return data.headerRow;
    }

    /**
     * Get the row number of the first data row.
     * 
     * @return firstDataRow Row number beginning at 1, or null.
     * @since 1.2
     */
    public Integer getFirstDataRow() {
        return data.firstDataRow;
    }

}
