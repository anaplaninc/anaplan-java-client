package com.anaplan.client.transport.decoders;

import com.anaplan.client.transport.serialization.AnaplanByteArrayDeserializer;
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
import java.util.Collection;

/**
 * A brute extension of the default JacksonDecoder, except the raw-type is checked here whether its a byte array, in
 * which case it is decoded promptly using the {@link AnaplanByteArrayDeserializer}.
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
        return new AnaplanByteArrayDeserializer().deserialize(body.asInputStream());
      } else {
        return decodeFromReader(body, response, javaType);
      }

    }
  }


  private Object decodeFromReader(final Response.Body body, final Response response, JavaType javaType) throws IOException {
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
        if (isCsvStringResponse(response) || isJsonStringResponse(response)) {
          return stringFromReader(reader);
        }
        return this.mapper.readValue(reader, javaType);
      }
    } catch (RuntimeJsonMappingException | IOException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else {
        throw e;
      }
    }
  }
  /**
   * Checks if the response is of csv type based on content-type headers
   *
   * @param response Response
   * @return True if it is CSV
   */
  private boolean isCsvStringResponse(Response response) {
    boolean isCsv = false;
    Collection<String> contentType = response.headers().get("content-type");
    if (contentType != null) {
      isCsv = contentType.stream()
          .anyMatch(ct -> ct.contains("text/csv"));
    }
    return isCsv;
  }

  /**
   * Checks if the response is of json type based on requested content-typ
   *
   * @param response Response
   * @return True if it is CSV
   */
  private boolean isJsonStringResponse(Response response) {
    boolean isJson = false;
    Collection<String> contentType = response.request().headers().get("Accept");
    if (contentType != null) {
      isJson = contentType.stream()
          .anyMatch(ct -> ct.contains("application/json"));
    }
    return isJson;
  }

  /**
   * Read a string from a Reader
   *
   * @param reader Reader
   * @return Result as a String
   * @throws IOException reader error
   */
  private String stringFromReader(Reader reader) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      stringBuilder.append(line).append("\n");
    }
    return stringBuilder.toString();
  }
}
