//   Copyright 2012 Anaplan Inc.
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

package com.anaplan.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Utilities for dealing with encoding
 */

public class EncodingUtils {

    private static final String CHARSET = "UTF-8";

    /**
     * URL Encodes the <code>value</code> and applies the XOR algorithm.
     *
     * @param value The value to be encoded.
     * @return The encoded value.
     * @throws UnsupportedEncodingException
     */
    public static String encodeAndXor(String value) throws UnsupportedEncodingException {
        String xored = convertViaXOR(value);
        return URLEncoder.encode(xored, CHARSET);
    }

    /**
     * URL Decodes the <code>value</code> and applies the XOR algorithm.
     *
     * @param value The value to be decoded.
     * @return The decoded value.
     * @throws UnsupportedEncodingException
     */
    public static String decodeAndXor(String value) throws UnsupportedEncodingException {
        String urlDecoded = URLDecoder.decode(value, CHARSET);
        return convertViaXOR(urlDecoded);
    }

    private static String convertViaXOR(String value) {
        final int key = 129;
        StringBuilder outSb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            c = (char) (c ^ key);
            outSb.append(c);
        }
        return outSb.toString();
    }
}
