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

package com.anaplan.client.ex;

/**
 * Superclass of API call failure types.
 */

public class AnaplanAPIException extends RuntimeException {
    /**
     * Create an exception with the specified message.
     */
    public AnaplanAPIException(String message) {
        super(message);
    }

    /**
     * Create an exception with the specified message and cause.
     */
    public AnaplanAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
