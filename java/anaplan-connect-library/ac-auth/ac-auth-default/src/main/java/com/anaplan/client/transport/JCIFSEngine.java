//   Copyright 2012 Anaplan Inc.
//
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

  private static Class<?> type1MESSAGE;
  private static Class<?> type2MESSAGE;
  private static Class<?> type3MESSAGE;
  private static Class<?> base64;
  private static int type1FLAGS;
  private static int ntlmsspTargetTypeDomain;
  private static int ntlmsspTargetTypeServer;
  private static final Throwable initFailure;

  static {
    Throwable failure = null;
    try {
      int type1Flags = 0;
      Class<?> ntlmFLAGS = Class.forName("jcifs.ntlmssp.NtlmFlags");
      type1MESSAGE = Class.forName("jcifs.ntlmssp.Type1Message");
      type2MESSAGE = Class.forName("jcifs.ntlmssp.Type2Message");
      type3MESSAGE = Class.forName("jcifs.ntlmssp.Type3Message");
      base64 = Class.forName("jcifs.util.Base64");
      ntlmsspTargetTypeDomain = ntlmFLAGS.getField("NTLMSSP_TARGET_TYPE_DOMAIN").getInt(null);
      ntlmsspTargetTypeServer = ntlmFLAGS.getField("NTLMSSP_TARGET_TYPE_SERVER").getInt(null);
      for (String flagName : Arrays.asList(
          "NTLMSSP_NEGOTIATE_56",
          "NTLMSSP_NEGOTIATE_128",
          "NTLMSSP_NEGOTIATE_NTLM2",
          "NTLMSSP_NEGOTIATE_ALWAYS_SIGN",
          "NTLMSSP_REQUEST_TARGET")) {
        type1Flags |= ntlmFLAGS.getField(flagName).getInt(null);
      }
      type1FLAGS = type1Flags;
    } catch (Exception thrown) {
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
      Object type1Message = type1MESSAGE.getConstructor(Integer.TYPE, String.class, String.class)
          .newInstance(type1FLAGS, domain, workstation);
      byte[] byteArray = (byte[]) type1MESSAGE.getMethod("toByteArray").invoke(type1Message);
      return (String) base64.getMethod("encode", byteArray.getClass()).invoke(null, byteArray);
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
      byte[] byteArray = (byte[]) base64.getMethod("decode", String.class).invoke(null, challenge);
      Object type2Message = type2MESSAGE.getConstructor(byteArray.getClass()).newInstance(byteArray);
      Integer type2Flags = (Integer) type2MESSAGE.getMethod("getFlags").invoke(type2Message);
      int type3Flags = type2Flags
          & (0xffffffff ^ (ntlmsspTargetTypeDomain | ntlmsspTargetTypeServer));
      Object type3Message = type3MESSAGE
          .getConstructor(type2MESSAGE, String.class, String.class, String.class, String.class, Integer.TYPE)
          .newInstance(type2Message, password, domain, username, workstation, type3Flags);
      byte[] type3ByteArray = (byte[]) type3MESSAGE.getMethod("toByteArray").invoke(type3Message);
      return (String) base64
          .getMethod("encode", type3ByteArray.getClass()).invoke(null, type3ByteArray);
    } catch (Exception thrown) {
      throw new NTLMEngineException("Failed to generate NTLM type 3 message", thrown);
    }
  }
}
