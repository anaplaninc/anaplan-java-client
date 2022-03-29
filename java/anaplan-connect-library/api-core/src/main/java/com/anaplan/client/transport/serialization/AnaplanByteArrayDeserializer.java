package com.anaplan.client.transport.serialization;

import com.anaplan.client.StreamUtils;
import java.io.IOException;
import java.io.InputStream;

/**
 * Deserializes any content of type byte[], which is usually when downloading file-chunks from the API.
 */
public class AnaplanByteArrayDeserializer {

  /**
   * Deserializes from character-stream to raw bytes. This is usually downloading data from the server, such as
   * file-chunks.
   *
   * @param inputStream stream to be deserialized
   * @return byte[]
   * @throws IOException if there's an error while closing the input stream
   */
  public byte[] deserialize(InputStream inputStream) throws IOException {
    return StreamUtils.inputStreamToByteArray(inputStream);
  }

}
