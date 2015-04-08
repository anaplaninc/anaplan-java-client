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

import com.google.gson.Gson;

/**
  * SerializationHandler implementation using Google's GSON library.
  */

public class GsonHandler implements SerializationHandler {

    private Gson gson = new Gson();

    public GsonHandler() {
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String className = getClass().getName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /** {@inheritDoc} */
    @Override
    public String getContentType() {
        return "application/json";
    }

    /** {@inheritDoc} */
    @Override
    public <T> byte[] serialize(T instance, TypeWrapper<T> typeWrapper) throws AnaplanAPISerializationException {
        try {
            String json = gson.toJson(instance, typeWrapper.getType());
            return json.getBytes("UTF-8");
        } catch (Throwable thrown) {
            throw new AnaplanAPISerializationException("Failed to serialize API request parameters using GSON", thrown);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, TypeWrapper<T> typeWrapper) throws AnaplanAPISerializationException {
        try {
            String json = new String(data, "UTF-8");
            return (T) gson.fromJson(json, typeWrapper.getType());
        } catch (Throwable thrown) {
            throw new AnaplanAPISerializationException("Failed to deserialize API response result using GSON", thrown);
        }
    }
}
