package com.anaplan.client.transport.serialization;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

/**
 * Deserializes any content of type byte[], which is usually when downloading file-chunks from
 * the API.
 */
public class ByteArrayDeserializer {

    /**
     * Deserializes from character-stream to raw bytes. This is usually downloading data from the server,
     * such as file-chunks.
     * @param inputStream
     * @return byte[]
     * @throws IOException if there's an error while closing the input stream
     */
    public byte[] deserialize(InputStream inputStream) throws IOException {
        byte[] data = ByteStreams.toByteArray(inputStream);
        inputStream.close();
        return data;
    }
}
