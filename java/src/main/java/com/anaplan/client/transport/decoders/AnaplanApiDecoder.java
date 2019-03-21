package com.anaplan.client.transport.decoders;

import com.anaplan.client.transport.serialization.ByteArrayDeserializer;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import feign.Response;
import feign.Util;
import feign.jackson.JacksonDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * A brute extension of the default JacksonDecoder, except the raw-type is checked here whether
 * its a byte array, in which case it is decoded promptly using the
 * {@link com.anaplan.client.transport.serialization.ByteArrayDeserializer}.
 */
public class AnaplanApiDecoder extends JacksonDecoder {
    private final ObjectMapper mapper;

    public AnaplanApiDecoder(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.status() == 404) {
            return Util.emptyValueOf(type);
        } else if (response.body() == null) {
            return null;
        } else {
            Response.Body body = response.body();

            JavaType javaType = this.mapper.constructType(type);

            if (javaType.isTypeOrSubTypeOf(byte[].class)) {
                return new ByteArrayDeserializer().deserialize(body.asInputStream());
            } else {
                Reader reader = body.asReader();

                if (!reader.markSupported()) {
                    reader = new BufferedReader(reader, 1);
                }

                try {
                    reader.mark(1);
                    if (reader.read() == -1) {
                        return null;
                    } else {
                        reader.reset();
                        return this.mapper.readValue(reader, javaType);
                    }
                } catch (RuntimeJsonMappingException e) {
                    if (e.getCause() != null && e.getCause() instanceof IOException) {
                        throw IOException.class.cast(e.getCause());
                    } else {
                        throw e;
                    }
                }
            }

        }
    }
}
