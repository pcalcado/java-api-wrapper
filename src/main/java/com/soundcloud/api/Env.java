package com.soundcloud.api;

import org.apache.http.HttpHost;

/**
 * The environment to operate against.
 * Use SANDBOX for testing your app, and LIVE for production applications.
 */
@SuppressWarnings({"UnusedDeclaration"})
public enum Env {
    /** The main production site, http://soundcloud.com */
    LIVE("api.soundcloud.com", "soundcloud.com"),
    /** For testing, http://sandbox-soundcloud.com */
    SANDBOX("api.sandbox-soundcloud.com", "sandbox-soundcloud.com");

    public final HttpHost apiHost, apiSslHost, webHost, webSslHost;

    /**
     * @param apiHostName           the api host
     * @param resourceHostName      the resource (=web host)
     */
    Env(String apiHostName, String resourceHostName) {
        apiHost = new HttpHost(apiHostName, -1, "http");
        apiSslHost = new HttpHost(apiHostName, -1, "https");

        webHost = new HttpHost(resourceHostName, -1, "http");
        webSslHost = new HttpHost(resourceHostName, -1, "https");
    }

    public HttpHost getApiHost(boolean secure) {
        return secure ? apiSslHost : apiHost;
    }

    public HttpHost getWebHost(boolean secure) {
        return secure ? webSslHost : webHost;
    }
}
