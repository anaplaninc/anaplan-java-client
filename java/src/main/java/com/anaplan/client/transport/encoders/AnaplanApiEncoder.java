package com.anaplan.client.transport.encoders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.jackson.JacksonEncoder;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * A brute extension of the default JacksonEncoder to use the Server Encoding and remove the enforced UTF-8 encoding
 * {@link com.anaplan.client.transport.serialization.ByteArraySerializer}.
 */

public class AnaplanApiEncoder extends JacksonEncoder {

    private final ObjectMapper mapper;

    public AnaplanApiEncoder(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) {
        try {
            JavaType javaType = mapper.getTypeFactory().constructType(bodyType);
            template.body(mapper.writerFor(javaType).writeValueAsString(object).getBytes(Charset.forName(System.getProperty("file.encoding"))), Charset.forName(System.getProperty("file.encoding")));
        } catch (JsonProcessingException e) {
            throw new EncodeException(e.getMessage(), e);
        }
    }
}