package com.anaplan.client;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;

/**
 * CryptoUtil class is used for encrypting and decryption
 */
public class CryptoUtil {
  private static final byte[] KEY = { 122, 120, 101, 87, 32, 43, 69, 83, 101, 54, 82, 23, 116, 75, 101, 66 };
  private static final String SALT = "P@$word$@|t";

  /**
   * @param value Value to encrypt
   * @return encrypted value
   */
  public static String encrypt(final String value) {
    return encrypt(value, SALT);
  }

  /**
   * @param value Value to encrypt
   * @param salt salt to add to encryption
   * @return encrypted value
   */
  public static String encrypt(final String value, final String salt) {
    if (StringUtils.isBlank(value)) {
      throw new AnaplanCyptoException("Empty value to encrypt.");
    }
    try {
      Cipher cipher = getCipherInstance();
      final SecretKeySpec secretKey = new SecretKeySpec(KEY, "AES");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] encryptedText = cipher.doFinal(salt.concat(value).getBytes());
      return Base64.getEncoder().encodeToString(encryptedText);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
      throw new AnaplanCyptoException("Unable to encrypt value", e);
    }
  }

  /**
   * @param encodedValue Value to decrypt
   * @return decrypted value
   */
  public static String decrypt(final String encodedValue) {
    return decrypt(encodedValue, SALT);
  }


  /**
   * @param encodedValue Value to decrypt
   * @param salt salt to add to encryption
   * @return decrypted value
   */
  public static String decrypt(final String encodedValue, final String salt) {
    if (StringUtils.isBlank(encodedValue)) {
      throw new AnaplanCyptoException("Empty value to decrypt");
    }
    try {
      Cipher cipher = getCipherInstance();
      final SecretKeySpec secretKey = new SecretKeySpec(KEY, "AES");
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      byte[] encryptedText = Base64.getDecoder().decode(encodedValue.getBytes());
      return new String(cipher.doFinal(encryptedText)).substring(SALT.length());
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
      throw new AnaplanCyptoException("Unable to encrypt value", e);
    }
  }

  private static Cipher getCipherInstance() throws NoSuchAlgorithmException, NoSuchPaddingException {
    return Cipher.getInstance("AES/ECB/PKCS5PADDING");
  }
}
