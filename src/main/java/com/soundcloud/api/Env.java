package com.soundcloud.api;

import org.apache.http.HttpHost;

/**
 * The environment to operate against.
 * Use SANDBOX for testing your app, and LIVE for production applications.
 */
@SuppressWarnings({"UnusedDeclaration"})
public enum Env {
    /** The main production site, http://soundcloud.com */
    LIVE("api.soundcloud.com"),
    /** For testig, http://sandbox-soundcloud.com */
    SANDBOX("api.sandbox-soundcloud.com");

    public final HttpHost host, sslHost;
    Env(String hostname) {
        host = new HttpHost(hostname, -1, "http");
        sslHost = new HttpHost(hostname, -1, "https");
    }
}
