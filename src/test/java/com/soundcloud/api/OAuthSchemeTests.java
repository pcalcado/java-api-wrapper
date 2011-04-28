package com.soundcloud.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.Header;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class OAuthSchemeTests {
    OAuthScheme scheme;
    CloudAPI api;

    @Before
    public void setup() {
        api = mock(CloudAPI.class);
        scheme = new OAuthScheme(api, null);
    }

    @Test
    public void shouldRefreshTokenOnAuthenticate() throws Exception {
        when(api.getToken()).thenReturn(null);
        when(api.refreshToken()).thenReturn(new Token("1", "2"));
        scheme.authenticate(null, null);
        verify(api).invalidateToken();
        verify(api).refreshToken();
    }

    @Test
    public void shouldSetCorrectHeaderOnAuthenticate() throws Exception {
        when(api.getToken()).thenReturn(new Token("myt0k3n", "r3fr3sh"));
        when(api.refreshToken()).thenReturn(new Token("1", "2"));

        Header header = scheme.authenticate(null, null);
        assertNotNull(header);

        assertThat(header.getName(), equalTo("Authorization"));
        assertThat(header.getValue(), equalTo("OAuth myt0k3n"));
    }

    @Test
    public void shouldInvaliateTokenOnAuthenticate() throws Exception {
        when(api.refreshToken()).thenReturn(new Token("1", "2"));
        scheme.authenticate(null, null);
        verify(api).invalidateToken();
    }

    @Test(expected = AuthenticationException.class)
    public void shouldRethrowIOExceptionAsAuthenticationException() throws Exception {
        when(api.refreshToken()).thenThrow(new IOException("broken"));
        scheme.authenticate(null, null);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldRethrowIllegalStateExceptionAsAuthenticationException() throws Exception {
        when(api.refreshToken()).thenThrow(new IllegalStateException());
        scheme.authenticate(null, null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotCatchAllExceptions() throws Exception {
        when(api.refreshToken()).thenThrow(new RuntimeException());
        scheme.authenticate(null, null);
    }

    @Test
    public void processChallengeShouldParseHeaderCorrectly() throws Exception {
        scheme.processChallenge(
                new BasicHeader("WWW-Authenticate",
                        "OAuth realm=\"SoundCloud\", error=\"invalid_request\""));

        assertThat(scheme.getRealm(), equalTo("SoundCloud"));
        assertThat(scheme.getParameter("error"), equalTo("invalid_request"));
    }

    @Test(expected = MalformedChallengeException.class)
    public void shouldThrowMalformedChallengeException() throws Exception {
        scheme.processChallenge(
                new BasicHeader("WWW-BlaFargh",
                        "OAuth realm=\"SoundCloud\", error=\"invalid_request\""));
    }

    @Test
    public void shouldHaveCorrectSchemeName() throws Exception {
        assertThat(scheme.getSchemeName(), equalTo("oauth"));
    }

    @Test
    public void shouldNotBeConnectionBase() throws Exception {
        assertThat(scheme.isConnectionBased(), is(false));
    }

    @Test
    public void shouldAlwaysBeComplete() throws Exception {
        assertThat(scheme.isComplete(), is(true));
    }

    @Test
    public void shouldExtractToken() throws Exception {
        assertThat(OAuthScheme.extractToken(new BasicHeader(AUTH.WWW_AUTH_RESP, "OAuth 1234")), equalTo("1234"));
        assertThat(OAuthScheme.extractToken(new BasicHeader("Random", "OAuth 1234")), nullValue());
        assertThat(OAuthScheme.extractToken(new BasicHeader(AUTH.WWW_AUTH_RESP, "Foo 1234")), nullValue());
    }
}
