package com.anaplan.client.transport.encoders;

import static com.anaplan.client.api.AnaplanAPI.URL_CHUNK_ID;
import static com.anaplan.client.api.AnaplanAPI.URL_IMPORT_TASKS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.jackson.JacksonEncoder;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A brute extension of the default JacksonEncoder to use the Server Encoding and remove the
 * enforced UTF-8 encoding {@link com.anaplan.client.transport.serialization.AnaplanByteArraySerializer}.
 */

public class AnaplanApiEncoder extends JacksonEncoder {

  private final ObjectMapper mapper;

  public AnaplanApiEncoder(ObjectMapper objectMapper) {
    this.mapper = objectMapper;
  }

  @Override
  public void encode(Object object, Type bodyType, RequestTemplate template) {
    Charset charset = getCharset(template);
    if (charset != null) {
      JavaType javaType = mapper.getTypeFactory().constructType(bodyType);
      try {
        String value = mapper.writerFor(javaType).writeValueAsString(object);
        template.body(value.getBytes(charset), charset);
      } catch (JsonProcessingException e) {
        throw new EncodeException(e.getMessage(), e);
      }
    }
    else {
      template.body((byte[]) object, null);
    }
  }

  /**
   * Uses system property to get the charset with the exception of a configured url
   *
   * @param template {@link RequestTemplate}
   * @return The charset
   */
  private Charset getCharset(RequestTemplate template) {
    Charset charset;
    if (URL_CHUNK_ID.equals(template.url())) {
      charset = null;
    } else if (URL_IMPORT_TASKS.equals(template.url())) {
      charset = StandardCharsets.UTF_8;
    } else {
      charset = Charset.forName(System.getProperty("file.encoding"));
    }
    return charset;
  }
}
