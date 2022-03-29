package com.anaplan.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.anaplan.client.transport.serialization.AnaplanByteArraySerializer;

//TODO: create abstractions for this and other jackson dependencies and remove the jackson jar dependencies

/**
 * Provides the json Object mapper
 */
public class ObjectMapperProvider {

  private ObjectMapper objectMapper = createObjectMapper();

  private ObjectMapperProvider() {
  }

  public static ObjectMapper getObjectMapper() {
    return InstanceHolder.SINGLETON_INSTANCE.objectMapper;
  }

  /**
   * Customizes the object-mapper to use a custom file-chunk-serializer. Also sets the visibility for parsing to
   * field-names only, so getter and setter names are ignored.
   *
   * @return {@link ObjectMapper}
   */
  private static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    SimpleModule byteSerializerMod = new SimpleModule();
    byteSerializerMod.addSerializer(new AnaplanByteArraySerializer(byte[].class));
    objectMapper.registerModule(byteSerializerMod)
        .setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper;
  }

  private static class InstanceHolder {

    private static final ObjectMapperProvider SINGLETON_INSTANCE = new ObjectMapperProvider();
  }

}
