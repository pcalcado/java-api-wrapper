package com.soundcloud.api;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * Interface with SoundCloud, using OAuth2.
 * This API wrapper makes a few assumptions - namely:
 * <ul>
 *     <li>Only resource owner passwords credentials is supported</li>
 *     <li>Server responses are always requested in JSON format</li>
 *     <li>Refresh-token handling is transparent to the client application</li>
 * </ul>
 *
 * This is the actual interface, for the implementation see ApiWrapper.
 *
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
     * Request login/signup via Facebook.
     * After the Facebook login, control will go to the redirect URI (wrapper specific).
     * @return the URI to open in a browser/WebView etc.
     */
    URI loginViaFacebook();

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
     * Request a signup token using <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-15#section-4.4">
     * Client Credentials</a>. Note that not all apps are allowed to use this token type.
     * @return a valid token
     * @throws IOException IO/Error
     * @throws com.soundcloud.api.CloudAPI.InvalidTokenException if requested scope is not available
     */
    Token signupToken() throws IOException;

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
     * @param resource resource to GET
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse getContent(String resource) throws IOException;

    /**
     * @param resource resource to GET
     * @param params query parameters
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse getContent(String resource, Params params) throws IOException;

    /**
     * @param resource resource to POST
     * @param params query parameters
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse postContent(String resource, Params params) throws IOException;

    /**
     * @param resource resource to PUT
     * @param params query parameters
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse putContent(String resource, Params params) throws IOException;

    /**
     * @param resource resource to DELETE
     * @return the HTTP response
     * @throws IOException IO/Error
     */
    HttpResponse deleteContent(String resource) throws IOException;

    /** Adds current access token to url
     * @param url url to be signed
     * @return signed url
     */
    @Deprecated String signUrl(String url);

    /**
     * Resolve the given SoundCloud URI
     *
     * @param uri SoundCloud model URI, e.g. http://soundcloud.com/bob
     * @return the id or -1 if not resolved successfully
     * @throws IOException network errors
     */
    long resolve(String uri) throws IOException;

    Token getToken();
    void setToken(Token token);
    void addTokenStateListener(TokenStateListener listener);

    enum Env {
        LIVE("api.soundcloud.com"),
        SANDBOX("api.sandbox-soundcloud.com");
        public final HttpHost host, sslHost;

        Env(String hostname) {
            host = new HttpHost(hostname, -1, "http");
            sslHost = new HttpHost(hostname, -1, "https");
        }
    }


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

    class InvalidTokenException extends IOException {
        public InvalidTokenException(int code, String s) {
            super("HTTP error:" + code + " (" + s + ")");
        }
    }
}
