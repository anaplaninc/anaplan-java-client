package com.anaplan.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CryptoUtilTest {

  @Test
  void shouldEncryptValue() {
    String encryptedValue = CryptoUtil.encrypt("SomePassword");
    assertThat(encryptedValue, notNullValue());
    String decryptedValue = CryptoUtil.decrypt(encryptedValue);
    assertThat(decryptedValue, notNullValue());
    assertThat(decryptedValue, equalTo("SomePassword"));
  }

  @Test
  void shouldThrowExceptionForEmptyValue() {
    AnaplanCyptoException thrown = assertThrows(
        AnaplanCyptoException.class,
        () -> CryptoUtil.encrypt("    "),
        "Expected encrypt() to throw exception, but it didn't"
    );
    assertThat(thrown.getMessage(), is("Empty value to encrypt."));
  }

}
