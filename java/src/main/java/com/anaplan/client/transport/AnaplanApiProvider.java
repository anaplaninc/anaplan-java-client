package com.anaplan.client.transport;

import com.anaplan.client.Constants;
import com.anaplan.client.Version;
import com.anaplan.client.api.AnaplanAPI;
import com.anaplan.client.auth.Authenticator;
import com.anaplan.client.auth.Credentials;
import com.anaplan.client.transport.decoders.AnaplanApiDecoder;
import com.anaplan.client.transport.encoders.AnaplanApiEncoder;
import com.anaplan.client.transport.interceptors.AConnectHeaderInjector;
import com.anaplan.client.transport.interceptors.AuthTokenInjector;
import com.anaplan.client.transport.interceptors.CompressPutBodyInjector;
import com.anaplan.client.transport.interceptors.UserAgentInjector;
import com.anaplan.client.transport.retryer.AnaplanErrorDecoder;
import com.anaplan.client.transport.retryer.FeignApiRetryer;
import com.anaplan.client.transport.serialization.ByteArraySerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import feign.Client;
import feign.Feign;
import feign.okhttp.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Manages the Anaplan API connection with the help of Feign and Jackson encoder/decoders. Raw
 * bytes from downloading file-chunks or uploading raw bytes of file-chunks, are managed by their
 * respective {@link com.anaplan.client.transport.serialization.ByteArraySerializer} and
 * {@link com.anaplan.client.transport.serialization.ByteArrayDeserializer} classes. If proxy details
 * are provided, then tunnels the API connection through it using either regular or NTLM proxy.
 */
public class AnaplanApiProvider implements TransportApi {

    private static final Logger LOG = LoggerFactory.getLogger(AnaplanApiProvider.class);
    private static final String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";
    private final Authenticator authenticator;
    private ConnectionProperties properties;
    private AnaplanAPI apiClient;
    private ObjectMapper objectMapper;

    public AnaplanApiProvider(ConnectionProperties connectionProperties, Authenticator authenticator) {
        this.properties = connectionProperties;
        this.authenticator = authenticator;
    }

    public Credentials getCredentials() {
        return properties.getApiCredentials();
    }

    /**
     * Customizes the object-mapper to use a custom file-chunk-serializer. Also sets the visibility
     * for parsing to field-names only, so getter and setter names are ignored.
     * @return
     */
    public ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            SimpleModule byteSerializerMod = new SimpleModule();
            byteSerializerMod.addSerializer(new ByteArraySerializer(byte[].class));
            objectMapper.registerModule(byteSerializerMod)
                    .setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return objectMapper;
    }

    /**
     * Generates the Feign client for communicating with Anaplan APIs
     * @return
     */
    @Override
    public AnaplanAPI getApiClient() {
        if (apiClient == null) {
            apiClient = Feign.builder()
                    .client(createFeignClient())
                    .encoder(new AnaplanApiEncoder(getObjectMapper()))
                    .decoder(new AnaplanApiDecoder(getObjectMapper()))
                    .requestInterceptors(Arrays.asList(
                            new AuthTokenInjector(authenticator),
                            new UserAgentInjector(),
                            new AConnectHeaderInjector(),
                            new CompressPutBodyInjector()))
                    .retryer(new FeignApiRetryer(
                            (long) (properties.getRetryTimeout() * 1000),
                            (long) Constants.MAX_RETRY_TIMEOUT_SECS * 1000,
                            properties.getMaxRetryCount(),
                            FeignApiRetryer.DEFAULT_BACKOFF_MULTIPLIER))
                    .errorDecoder(new AnaplanErrorDecoder())
                    .target(AnaplanAPI.class, properties.getApiServicesUri().toString() + "/" + Version.API_MAJOR + "/" + Version.API_MINOR);
        }
        return apiClient;
    }

    @Override
    public void setApiClient(AnaplanAPI anaplanAPI) {
        this.apiClient = anaplanAPI;
    }

    /**
     * Creates a Feign/OkHttp client for speaking to Auth-Service and sets up the
     * appropriate proxy handler.
     *
     * @return A Feign/OkHttp client
     */
    protected Client createFeignClient() {
        okhttp3.OkHttpClient.Builder okHttpBuilder = new okhttp3.OkHttpClient.Builder();
        if (properties.getProxyLocation() != null) {
            LOG.info("Setting up proxy...");
            setupProxy(okHttpBuilder);
        }
        // setting the read and write timeouts as well
        okHttpBuilder.connectTimeout(properties.getHttpTimeout(), TimeUnit.SECONDS)
        .readTimeout(properties.getHttpTimeout(),TimeUnit.SECONDS)
        .writeTimeout(properties.getHttpTimeout(),TimeUnit.SECONDS);
        return new OkHttpClient(okHttpBuilder.build());
    }

    /**
     * Sets up an NTLM proxy or a regular proxy based on credential types.
     *
     * @param okHttpBuilder
     */
    private void setupProxy(okhttp3.OkHttpClient.Builder okHttpBuilder) {
        Credentials proxyCredentials = properties.getProxyCredentials();

        okHttpBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                properties.getProxyLocation().getHost(),
                properties.getProxyLocation().getPort())));

        if (proxyCredentials != null) {
            if (proxyCredentials.isNtlm()) {
                okHttpBuilder
                        .proxyAuthenticator(new NtlmAuthenticator(
                                proxyCredentials.getUserName(),
                                proxyCredentials.getPassPhrase(),
                                proxyCredentials.getDomain(),
                                proxyCredentials.getWorkstation()));
            } else {
                okHttpBuilder
                        .proxyAuthenticator((route, response) -> response.request().newBuilder()
                                .header("Connection","close")
                                .header(PROXY_AUTHORIZATION_HEADER, okhttp3.Credentials.basic(
                                        proxyCredentials.getUserName(),
                                        proxyCredentials.getPassPhrase()))
                                .build());
            }
        }
    }
}
