package com.soundcloud.api;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * Interface with SoundCloud, using OAuth2.
 * This API wrapper makes a few assumptions - namely:
 * <ul>
 *     <li>Server responses are always requested in JSON format</li>
 *     <li>Refresh-token handling is transparent to the client application</li>
 * </ul>
 *
 * This is the interface, for the implementation see ApiWrapper.
 * @version 1.0
 * @author Jan Berkel <jan@soundcloud.com>
 * @see ApiWrapper
 */
public interface CloudAPI {
    // grant types
    String PASSWORD           = "password";
    String AUTHORIZATION_CODE = "authorization_code";
    String REFRESH_TOKEN      = "refresh_token";
    String OAUTH1_TOKEN       = "oauth1_token";
    String CLIENT_CREDENTIALS = "client_credentials";

    // other constants
    String REALM              = "SoundCloud";
    String OAUTH_SCHEME       = "oauth";
    String VERSION            = "1.0";
    String USER_AGENT         = "SoundCloud Java Wrapper "+ VERSION;

    /**
     * Log in to SoundCloud using <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.2">
     * Resource Owner Password Credentials</a>
     *
     * @param username SoundCloud username
     * @param password SoundCloud password
     * @return a valid token
     * @throws com.soundcloud.api.CloudAPI.InvalidTokenException invalid token
     * @throws IOException In case of network/server errors
     */
    Token login(String username, String password) throws IOException;


    /**
     * Log in to SoundCloud using <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.1">
     * Authorization Code</a>
     *
     * @param code the authorization code
     * @return a valid token
     * @throws com.soundcloud.api.CloudAPI.InvalidTokenException invalid token
     * @throws IOException In case of network/server errors
     */
    Token authorizationCode(String code) throws IOException;

    /**
     * Request a "signup" token using <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-15#section-4.4">
     * Client Credentials</a>.
     *
     * Note that this token is <b>not</b> set as the current token in the wrapper - it should only be used
     * for one request (typically the signup / user creation request).
     * Also note that not all apps are allowed to use this token type.
     *
     * @return a valid token
     * @throws IOException IO/Error
     * @throws com.soundcloud.api.CloudAPI.InvalidTokenException if requested scope is not available
     */
    Token clientCredentials() throws IOException;

    /**
     * Tries to refresh the currently used access token with the refresh token
     * @return a valid token
     * @throws IOException in case of network problems
     * @throws com.soundcloud.api.CloudAPI.InvalidTokenException invalid token
     * @throws IllegalStateException if no refresh token present
     */
    Token refreshToken() throws IOException;

    /**
     * Exchange an OAuth1 Token for new OAuth2 tokens
     * @param oauth1AccessToken a valid OAuth1 access token, registered with the same client
     * @return a valid token
     * @throws IOException IO/Error
     * @throws InvalidTokenException Token error
     */
    Token exchangeToken(String oauth1AccessToken) throws IOException;

    /** Called to invalidate the current token */
    void invalidateToken();

    /**
     * @param request resource to GET
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse get(Request request) throws IOException;

    /**
     * @param request resource to POST
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse post(Request request) throws IOException;

    /**
     * @param request resource to PUT
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse put(Request request) throws IOException;

    /**
     * @param request resource to DELETE
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse delete(Request request) throws IOException;

    /**
     * Resolve the given SoundCloud URI
     *
     * @param uri SoundCloud model URI, e.g. http://soundcloud.com/bob
     * @return the id or -1 if not resolved successfully
     * @throws IOException network errors
     */
    long resolve(String uri) throws IOException;

    /** Obtain the current token */
    Token getToken();

    /** Set the current token used by the wrapper */
    void setToken(Token token);

    void addTokenStateListener(TokenStateListener listener);

    /**
     * Request login/signup via Facebook.
     * After the Facebook login, control will go to the redirect URI (wrapper specific), with
     * one of the following query parameters appended:
     * <ul>
     * <li><code>code</code> in case of success, this will contain the code used for the
     *     <code>authorizationCode</code> call to obtain the access token.
     * <li><code>error</code> in case of failure, this contains an error code (most likely
     * <code>access_denied</code>).
     * </ul>
     * @return the URI to open in a browser/WebView etc.
     * @see CloudAPI#authorizationCode(String)
     */
    URI loginViaFacebook();

    /**
     * The environment to operate against.
     * Use SANDBOX for testing your app, and LIVE for production applications.
     */
    enum Env {
        /** The main production site */
        @SuppressWarnings({"UnusedDeclaration"})
        LIVE("api.soundcloud.com"),
        /** sandbox-soundcloud.com */
        SANDBOX("api.sandbox-soundcloud.com");
        public final HttpHost host, sslHost;

        Env(String hostname) {
            host = new HttpHost(hostname, -1, "http");
            sslHost = new HttpHost(hostname, -1, "https");
        }
    }

    /**
     * Interested in changes to the current token.
     */
    interface TokenStateListener {
        /**
         * Called when token was found to be invalid
         * @param token the invalid token
         */
        void onTokenInvalid(Token token);

        /**
         * Called when the token got successfully refreshed
         * @param token      the refreshed token
         */
        void onTokenRefreshed(Token token);
    }

    /**
     * Thrown when token is not valid.
     */
    class InvalidTokenException extends IOException {
        private static final long serialVersionUID = 1954919760451539868L;

        /**
         * @param code the HTTP error code
         * @param status the HTTP status, or other error message
         */
        public InvalidTokenException(int code, String status) {
            super("HTTP error:" + code + " (" + status + ")");
        }
    }
}
