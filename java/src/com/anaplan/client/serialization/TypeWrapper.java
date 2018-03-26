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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
  * Provides the means to pass a complete type to the Java runtime.
  * The type is introduced in the type parameter T, and accessed using getType()
  * This arrangement prevents the actual parameters of a generic type being lost
  * by erasure.
  */
public abstract class TypeWrapper<T> {
    private final Type type;
    protected TypeWrapper() {
        Type superType = getClass().getGenericSuperclass();
        ParameterizedType parameterizedSuperType
                = (ParameterizedType) superType;
        type = parameterizedSuperType.getActualTypeArguments()[0];
    }
    /**
      * Return an instance of Type representing the type.
      */
    public final Type getType() {
        return type;
    }
}

