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

package com.anaplan.client.transport;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;

/**
 * Utilizes JCIFS for NTLM authentication.
 */
public class JCIFSEngine implements NTLMEngine {

    private static Class<?> NTLM_FLAGS, TYPE_1_MESSAGE, TYPE_2_MESSAGE, TYPE_3_MESSAGE, BASE64;
    private static int TYPE_1_FLAGS, NTLMSSP_TARGET_TYPE_DOMAIN, NTLMSSP_TARGET_TYPE_SERVER;
    private static final Throwable initFailure;

    static {
        Throwable failure = null;
        try {
            int type1Flags = 0;
            NTLM_FLAGS = Class.forName("jcifs.ntlmssp.NtlmFlags");
            TYPE_1_MESSAGE = Class.forName("jcifs.ntlmssp.Type1Message");
            TYPE_2_MESSAGE = Class.forName("jcifs.ntlmssp.Type2Message");
            TYPE_3_MESSAGE = Class.forName("jcifs.ntlmssp.Type3Message");
            BASE64 = Class.forName("jcifs.util.Base64");
            NTLMSSP_TARGET_TYPE_DOMAIN = NTLM_FLAGS.getField("NTLMSSP_TARGET_TYPE_DOMAIN").getInt(null);
            NTLMSSP_TARGET_TYPE_SERVER = NTLM_FLAGS.getField("NTLMSSP_TARGET_TYPE_SERVER").getInt(null);
            for (String flagName : Arrays.asList(
                    "NTLMSSP_NEGOTIATE_56",
                    "NTLMSSP_NEGOTIATE_128",
                    "NTLMSSP_NEGOTIATE_NTLM2",
                    "NTLMSSP_NEGOTIATE_ALWAYS_SIGN",
                    "NTLMSSP_REQUEST_TARGET")) {
                type1Flags |= NTLM_FLAGS.getField(flagName).getInt(null);
            }
            TYPE_1_FLAGS = type1Flags;
        } catch (Throwable thrown) {
            failure = thrown;
        } finally {
            initFailure = failure;
        }
    }

    JCIFSEngine() throws InvocationTargetException {
        if (null != initFailure) {
            if (initFailure instanceof InvocationTargetException) {
                throw (InvocationTargetException) initFailure;
            }
            throw new InvocationTargetException(initFailure,
                    "Cannot load JCIFSEngine");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateType1Msg(String domain, String workstation)
            throws NTLMEngineException {
        try {
            Object type1Message = TYPE_1_MESSAGE.getConstructor(Integer.TYPE, String.class, String.class).newInstance(TYPE_1_FLAGS, domain, workstation);
            byte[] byteArray = (byte[]) TYPE_1_MESSAGE.getMethod("toByteArray").invoke(type1Message);
            return (String) BASE64.getMethod("encode", byteArray.getClass()).invoke(null, byteArray);
        } catch (Exception thrown) {
            throw new NTLMEngineException("Failed to generate NTLM type 1 message", thrown);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateType3Msg(String username, String password,
                                   String domain, String workstation, String challenge)
            throws NTLMEngineException {
        if (domain == null) {
            throw new NTLMEngineException("Domain must be specified with user name");
        }
        try {
            byte[] byteArray = (byte[]) BASE64.getMethod("decode", String.class).invoke(null, challenge);
            Object type2Message = TYPE_2_MESSAGE.getConstructor(byteArray.getClass()).newInstance(byteArray);
            Integer type2Flags = (Integer) TYPE_2_MESSAGE.getMethod("getFlags").invoke(type2Message);
            int type3Flags = type2Flags
                    & (0xffffffff ^ (NTLMSSP_TARGET_TYPE_DOMAIN | NTLMSSP_TARGET_TYPE_SERVER));
            Object type3Message = TYPE_3_MESSAGE.getConstructor(TYPE_2_MESSAGE, String.class, String.class, String.class, String.class, Integer.TYPE).newInstance(type2Message, password, domain, username, workstation, type3Flags);
            byte[] type3ByteArray = (byte[]) TYPE_3_MESSAGE.getMethod("toByteArray").invoke(type3Message);
            return (String) BASE64.getMethod("encode", type3ByteArray.getClass()).invoke(null, type3ByteArray);
        } catch (Exception thrown) {
            throw new NTLMEngineException("Failed to generate NTLM type 3 message", thrown);
        }
    }
}
