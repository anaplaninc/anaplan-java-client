//   Copyright 2011 Anaplan Inc.
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

package com.anaplan.client.serialization;

/**
  * Represents a means of (un)marshalling parameters and results
  */
public interface SerializationHandler {
    /**
      * Return the content type produced/consumed.
      * This should normally be a recognized Internet media type as per
      * <nobr><a href="http://www.iana.org/assignments/media-types/">
      * http://www.iana.org/assignments/media-types/</a></nobr>.<br/>
      * Currently the Server can consume/produce both application/json and
      * application/xml.
      */
    String getContentType();
    /**
      * Serialize the request body of the specified type
      * @param <T> the type of object being serialized
      * @param instance The instance to serialize
      * @param type The type wrapper containing the type
      * @return A byte array containing the serialized content
      */
    <T> byte[] serialize(T instance, TypeWrapper<T> type)
            throws AnaplanAPISerializationException;
    /**
      * Deserialize the response body to the specified type
      * @param <T> the type of object being deserialized
      * @param data The byte array containing the serialized content
      * @param type The type wrapper containing the type
      * @return A newly-created object of the specified type
      */
    <T> T deserialize(byte[] data, TypeWrapper<T> type)
            throws AnaplanAPISerializationException;
}
