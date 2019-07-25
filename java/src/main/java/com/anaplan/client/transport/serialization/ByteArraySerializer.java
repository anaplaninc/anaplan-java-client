package com.anaplan.client.transport.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This helps to serialize the raw bytes of file-chunk into an UTF-8 encoded string for upload.
 */
public class ByteArraySerializer extends StdSerializer<byte[]> {

    public ByteArraySerializer(Class<byte[]> t) {
        super(t);
    }

    /**
     * Writes the raw-value as a UTF-8 encoded string.
     *
     * @param bytes
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(byte[] bytes, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeRawValue(new String(bytes, Charset.forName(System.getProperty("file.encoding"))));
    }
}
