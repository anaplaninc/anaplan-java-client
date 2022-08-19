package com.anaplan.client.auth;

import com.anaplan.client.DeviceTypeToken;
import com.anaplan.client.FeignAuthenticationAPIProvider;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.dto.DeviceCodeInfo;
import com.anaplan.client.dto.OauthTokenInfo;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.client.OkHttpFeignClientProvider;
import feign.Client;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.UUID;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceAuthenticatorTest {

  ConnectionProperties connectionProperties;
  AnaplanAuthenticationAPI authClient;
  OauthTokenInfo info;
  OauthTokenInfo infoRefresh;

  @BeforeEach
  void setup() {
    connectionProperties = new ConnectionProperties();
    connectionProperties.setClientId("id");
    connectionProperties.setAuthServiceUri(URI.create("auth_url"));
    connectionProperties.setMaxRetryCount(1);

    authClient = Mockito.mock(AnaplanAuthenticationAPI.class);
    DeviceCodeInfo codeInfo = new DeviceCodeInfo();
    codeInfo.setDeviceCode("code");

    info = new OauthTokenInfo();
    info.setRefreshToken("refresh");
    info.setAccessToken("access");
    info.setExpiresIn(1000);

    infoRefresh = new OauthTokenInfo();
    infoRefresh.setRefreshToken("re");
    infoRefresh.setAccessToken("aaa");
    infoRefresh.setExpiresIn(1000);

    when(authClient.deviceCode(argThat(new NotNullObject()), argThat(new NotNullObject()))).thenReturn(codeInfo);
    when(authClient.oauthToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject()))).thenReturn(info);
    when(authClient.oauthRefreshToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject())))
        .thenReturn(infoRefresh);
  }

  @Test
  void testAuthenticateWithForceNonRotatable()
      throws NoSuchFieldException, IllegalAccessException, InterruptedException, IOException, InvocationTargetException, NoSuchMethodException {
    ConnectionProperties connectionProperties = new ConnectionProperties();
    connectionProperties.setClientId("id");
    connectionProperties.setAuthServiceUri(URI.create("auth_url"));
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);

    try {
      // clear any previous tokens present
      deviceAuthenticator.clearRefreshTokenEntry();
      // authenticate the device and assert the token
      String firstAccessToken = "access_token_1";
      String firstRefreshToken = "refresh_token_1";
      info.setAccessToken(firstAccessToken);
      info.setRefreshToken(firstRefreshToken);
      byte[] response = deviceAuthenticator.authenticate();
      assertEquals(firstAccessToken, new String(response));

      // check token exists
      Field field = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      field.setAccessible(true);
      String tokenPath = (String) field.get(deviceAuthenticator);
      Files.exists(Paths.get(tokenPath));

      // check saved token
      Method method = deviceAuthenticator.getClass().getDeclaredMethod("getDecodedRefreshToken");
      method.setAccessible(true);
      String savedToken = (String) method.invoke(deviceAuthenticator);
      assertThat(firstRefreshToken, is(savedToken));
      FileTime ft1 = Files.getLastModifiedTime(Paths.get(tokenPath));
      connectionProperties.setForceRegister(true);
      Thread.sleep(2000L);

      // set values of oauth response
      String secondAccessToken = "access_token_2";
      String secondRefreshToken = "refresh_token_2";
      info.setAccessToken(secondAccessToken);
      info.setRefreshToken(secondRefreshToken);

      // 2nd invocation, creates new file and a new token in keystore
      response = deviceAuthenticator.authenticate();
      assertEquals(secondAccessToken, new String(response));

      // check token exists
      field = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      field.setAccessible(true);
      tokenPath = (String) field.get(deviceAuthenticator);
      Files.exists(Paths.get(tokenPath));

      // check saved token
      method = deviceAuthenticator.getClass().getDeclaredMethod("getDecodedRefreshToken");
      method.setAccessible(true);
      savedToken = (String) method.invoke(deviceAuthenticator);
      assertThat(secondRefreshToken, is(savedToken));
      FileTime ft2 = Files.getLastModifiedTime(Paths.get(tokenPath));
      verify(authClient, times(0)).
          oauthRefreshToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject()));
      assertThat(ft1.toMillis(), not(ft2.toMillis()));

      // 3rd invocation, not create a new file or modifies the file and should not create a new token in keystore
      response = deviceAuthenticator.authenticate();
      assertEquals(infoRefresh.getAccessToken(), new String(response));

      // check token exists
      field = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      field.setAccessible(true);
      tokenPath = (String) field.get(deviceAuthenticator);
      Files.exists(Paths.get(tokenPath));

      // check saved token
      method = deviceAuthenticator.getClass().getDeclaredMethod("getDecodedRefreshToken");
      method.setAccessible(true);
      savedToken = (String) method.invoke(deviceAuthenticator);
      assertThat(secondRefreshToken, is(savedToken));
      FileTime ft3 = Files.getLastModifiedTime(Paths.get(tokenPath));
      verify(authClient, times(1)).
          oauthRefreshToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject()));
      assertThat(ft2.toMillis(), is(ft3.toMillis()));
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testAuthenticateWithForceRotatable()
      throws NoSuchFieldException, IllegalAccessException, InterruptedException, IOException, InvocationTargetException, NoSuchMethodException {
    ConnectionProperties connectionProperties = new ConnectionProperties();
    connectionProperties.setClientId("id");
    connectionProperties.setRefreshType(DeviceTypeToken.ROTATABLE.name());
    connectionProperties.setAuthServiceUri(URI.create("auth_url"));
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);

    try {
      // clear any previous tokens present
      deviceAuthenticator.clearRefreshTokenEntry();

      // authenticate the device and assert the token
      String firstAccessToken = "access_token_1";
      String firstRefreshToken = "refresh_token_1";
      info.setAccessToken(firstAccessToken);
      info.setRefreshToken(firstRefreshToken);
      byte[] response = deviceAuthenticator.authenticate();
      assertEquals(firstAccessToken, new String(response));

      // check token exists
      Field field = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      field.setAccessible(true);
      String tokenPath = (String) field.get(deviceAuthenticator);
      Files.exists(Paths.get(tokenPath));

      // check saved token
      Method method = deviceAuthenticator.getClass().getDeclaredMethod("getDecodedRefreshToken");
      method.setAccessible(true);
      String savedToken = (String) method.invoke(deviceAuthenticator);
      assertThat(firstRefreshToken, is(savedToken));
      FileTime ft1 = Files.getLastModifiedTime(Paths.get(tokenPath));
      connectionProperties.setForceRegister(true);
      String secondAccessToken = "access_token_2";
      String secondRefreshToken = "refresh_token_2";
      info.setAccessToken(secondAccessToken);
      info.setRefreshToken(secondRefreshToken);
      Thread.sleep(2000L);

      // 2nd invocation, creates new file and a new token in keystore
      response = deviceAuthenticator.authenticate();
      assertEquals(secondAccessToken, new String(response));

      // check token exists
      field = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      field.setAccessible(true);
      tokenPath = (String) field.get(deviceAuthenticator);
      Files.exists(Paths.get(tokenPath));

      // check saved token
      method = deviceAuthenticator.getClass().getDeclaredMethod("getDecodedRefreshToken");
      method.setAccessible(true);
      savedToken = (String) method.invoke(deviceAuthenticator);
      assertThat(secondRefreshToken, is(savedToken));
      FileTime ft2 = Files.getLastModifiedTime(Paths.get(tokenPath));
      verify(authClient, times(0)).
          oauthRefreshToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject()));
      assertThat(ft1.toMillis(), not(ft2.toMillis()));
      Thread.sleep(2000L);

      // 3rd invocation, not create a new file or modifies the file and should not create a new token in keystore
      response = deviceAuthenticator.authenticate();
      assertEquals(infoRefresh.getAccessToken(), new String(response));

      // check token exists
      field = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      field.setAccessible(true);
      tokenPath = (String) field.get(deviceAuthenticator);
      Files.exists(Paths.get(tokenPath));

      // check saved token
      method = deviceAuthenticator.getClass().getDeclaredMethod("getDecodedRefreshToken");
      method.setAccessible(true);
      savedToken = (String) method.invoke(deviceAuthenticator);
      assertThat(infoRefresh.getRefreshToken(), is(savedToken));
      FileTime ft3 = Files.getLastModifiedTime(Paths.get(tokenPath));
      verify(authClient, times(1)).
          oauthRefreshToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject()));
      assertThat(ft2.toMillis(), not(ft3.toMillis()));
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testAuthenticateWithOutForce() throws NoSuchFieldException, IllegalAccessException, InterruptedException, IOException {
    ConnectionProperties connectionProperties = new ConnectionProperties();
    connectionProperties.setClientId("id");
    connectionProperties.setAuthServiceUri(URI.create("auth_url"));
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);
    try {
      // clear any previous tokens present
      deviceAuthenticator.clearRefreshTokenEntry();
      // authenticate the device and assert the token
      assertEquals("access", new String(deviceAuthenticator.authenticate()));
      Field field = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      field.setAccessible(true);
      String tokenPath = (String) field.get(deviceAuthenticator);
      Files.exists(Paths.get(tokenPath));
      FileTime ft1 = Files.getLastModifiedTime(Paths.get(tokenPath));
      Thread.sleep(1000L);
      deviceAuthenticator.authenticate();
      FileTime ft2 = Files.getLastModifiedTime(Paths.get(tokenPath));
      verify(authClient, times(1)).
          oauthRefreshToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject()));
      assertThat(ft1.toMillis(), is(ft2.toMillis()));
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testAuthenticateWithRefresh() {
    ConnectionProperties connectionProperties = new ConnectionProperties();
    connectionProperties.setClientId("id");
    connectionProperties.setAuthServiceUri(URI.create("auth_url"));
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);
    try {
      deviceAuthenticator.authenticate();
      //refresh the token with second method call
      assertEquals("aaa", new String(deviceAuthenticator.authenticate()));
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testRefreshToken() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);
    deviceAuthenticator.clearRefreshTokenEntry();
    try {
      byte[] response = deviceAuthenticator.refreshToken();
      assertThat(new String(info.getAccessToken().getBytes(StandardCharsets.UTF_8)), is(new String(response)));
      Field field = deviceAuthenticator.getClass().getSuperclass().getDeclaredField("authTokenExpiresAt");
      field.setAccessible(true);
      long reOld = (long) field.get(deviceAuthenticator);

      // Takes a time before refresh again
      deviceAuthenticator.authenticate();
      Thread.sleep(10);
      deviceAuthenticator.refreshToken();
      field = deviceAuthenticator.getClass().getSuperclass().getDeclaredField("authTokenExpiresAt");
      field.setAccessible(true);
      long reNew = (long) field.get(deviceAuthenticator);
      // Check if the new "authTokenExpiresAt" is older that the old "authTokenExpiresAt"
      assertTrue(reOld < reNew);
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testRefreshTokenExpire() {
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);
    try {
      deviceAuthenticator.refreshToken();
      assertEquals(1000, deviceAuthenticator.getOauthTokenInfo().getExpiresIn());
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testRefreshTokenCreationWithSuccess() {
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);
    try {
      assertFalse(new String(deviceAuthenticator.refreshToken()).isEmpty());
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testFailAuthenticate() {
    OkHttpFeignClientProvider okHttpClientProvider = new OkHttpFeignClientProvider();
    Supplier<Client> clientSupplier = () -> okHttpClientProvider.createFeignClient(connectionProperties);

    FeignAuthenticationAPIProvider authApiProvider = new FeignAuthenticationAPIProvider(connectionProperties,
        clientSupplier);
    authClient = authApiProvider.getAuthClient();
    connectionProperties.setClientId("aaaaa");
    connectionProperties.setAuthServiceUri(URI.create("fake"));
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);
    try {
      assertThrows(IllegalArgumentException.class, deviceAuthenticator::authenticate);
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testGetRefreshToken() {
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);
    try {
      OauthTokenInfo refreshToken = deviceAuthenticator.getAuthToken();
      assertEquals("refresh", refreshToken.getRefreshToken());
      assertEquals("access", refreshToken.getAccessToken());
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testGetRefreshTokenWithNoDeviceRegistration() {
    AnaplanAuthenticationAPI authClientTemp = Mockito.mock(AnaplanAuthenticationAPI.class);
    DeviceCodeInfo codeInfo = new DeviceCodeInfo();
    codeInfo.setDeviceCode("code");
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClientTemp);
    connectionProperties.setRetryTimeout(1);
    when(authClientTemp.deviceCode(argThat(new NotNullObject()), argThat(new NotNullObject()))).thenReturn(codeInfo);
    when(authClientTemp.oauthToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject()))).thenReturn(null);

    try {
      AnaplanAPIException anaplanAPIException = assertThrows(AnaplanAPIException.class, deviceAuthenticator::getAuthToken);
      assertTrue(anaplanAPIException.getMessage().startsWith("Application terminated."));
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testInAccessiblePathThrowsAPIException() throws NoSuchFieldException, IllegalAccessException { //
    ConnectionProperties connectionProperties = new ConnectionProperties();
    connectionProperties.setClientId("id");
    connectionProperties.setAuthServiceUri(URI.create("auth_url"));
    connectionProperties.setRetryTimeout(1);
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);

    try {
      Field path = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      path.setAccessible(true);

      path.set(deviceAuthenticator, String.format("%s/%s",System.getProperty("user.home"), UUID.randomUUID()));
      byte[] response = deviceAuthenticator.authenticate();
      assertEquals(new String(response), "access");
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testNoKeystoreExistsBefore() throws NoSuchFieldException, IllegalAccessException { //
    ConnectionProperties connectionProperties = new ConnectionProperties();
    connectionProperties.setClientId("id");
    connectionProperties.setAuthServiceUri(URI.create("auth_url"));
    connectionProperties.setRetryTimeout(1);
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);

    try {
      Field path = deviceAuthenticator.getClass().getDeclaredField("refreshTokenKeyStorePath");
      path.setAccessible(true);
      path.set(deviceAuthenticator, "/SomePath/NoExisting");
      AnaplanAPIException anaplanAPIException = assertThrows(AnaplanAPIException.class, deviceAuthenticator::authenticate);
      assertTrue(anaplanAPIException.getMessage().startsWith("Unable to save refresh token"));
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }

  @Test
  void testGetRefreshTokenWithRetry() {
    DeviceAuthenticator deviceAuthenticator = new DeviceAuthenticator(connectionProperties, authClient);
    try {
      when(
          authClient.oauthToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject())))
          .thenThrow(RuntimeException.class).thenReturn(info);
      OauthTokenInfo refreshToken = deviceAuthenticator.getAuthToken();
      assertEquals("refresh", refreshToken.getRefreshToken());
      assertEquals("access", refreshToken.getAccessToken());
      verify(authClient, times(2))
          .oauthToken(argThat(new NotNullObject()), argThat(new NotNullObject()), argThat(new NotNullObject()));
    } finally {
      deviceAuthenticator.clearRefreshTokenEntry();
    }
  }
}

class NotNullObject extends ArgumentMatcher<String> {
  public boolean matches(Object str) {
    return StringUtils.isNotBlank((CharSequence) str);
  }
}

