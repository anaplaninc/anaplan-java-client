package com.anaplan.client.transport.encoders;

import static com.anaplan.client.api.AnaplanAPI.URL_IMPORT_TASKS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.jackson.JacksonEncoder;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * A brute extension of the default JacksonEncoder to use the Server Encoding and remove the
 * enforced UTF-8 encoding {@link com.anaplan.client.transport.serialization.ByteArraySerializer}.
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
      Charset charset;
      String value = mapper.writerFor(javaType).writeValueAsString(object);
      charset = getCharset(template);
      template.body(value.getBytes(charset), charset);
    } catch (JsonProcessingException e) {
      throw new EncodeException(e.getMessage(), e);
    }
  }

  /**
   * Uses system property to get the charset with the exception of a configured url
   *
   * @param template
   * @return The charset
   */
  private Charset getCharset(RequestTemplate template) {
    Charset charset;
    if (URL_IMPORT_TASKS.equals(template.url())) {
      charset = Charset.forName("UTF-8");
    } else {
      charset = Charset.forName(System.getProperty("file.encoding"));
    }
    return charset;
  }
}