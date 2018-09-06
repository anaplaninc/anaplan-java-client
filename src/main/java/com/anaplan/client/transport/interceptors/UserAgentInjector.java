package com.anaplan.client.transport.interceptors;

import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.Version;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Injects the User-Agent header
 */
public class UserAgentInjector implements RequestInterceptor {

    private static final int MAJOR_VERSION = Version.API_MAJOR;
    private static final int MINOR_VERSION = Version.API_MINOR;
    private static final int REVISION_VERSION = Version.REVISION;
    private static final String RELEASE_VERSION = Version.RELEASE;

    @Override
    public void apply(RequestTemplate template) {
        template.header("User-Agent", buildUserAgentIdentifier());
    }

    /**
     * Generate a suitable value for a User-Agent header.
     */
    private String buildUserAgentIdentifier() throws AnaplanAPIException {
        StringBuilder result = new StringBuilder(getClass().getName());
        result.append("/").append(MAJOR_VERSION).append(".")
                .append(MINOR_VERSION);
        result.append(".").append(REVISION_VERSION).append(RELEASE_VERSION);
        String vmIdentifier = System.getProperty("java.vm.name") + " ("
                + System.getProperty("java.vendor") + ")/"
                + System.getProperty("java.vm.version") + " ("
                + System.getProperty("java.version") + ")";
        result.append("; ").append(vmIdentifier);
        String osIdentifier = System.getProperty("os.name") + " ("
                + System.getProperty("os.arch") + ")/"
                + System.getProperty("os.version");
        result.append("; ").append(osIdentifier).append(')');
        return result.toString();
    }
}
