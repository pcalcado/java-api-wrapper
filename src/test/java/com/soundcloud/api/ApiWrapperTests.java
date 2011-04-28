package com.soundcloud.api;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.soundcloud.api.fakehttp.FakeHttpLayer;
import com.soundcloud.api.fakehttp.RequestMatcher;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;


public class ApiWrapperTests {
    private ApiWrapper api;
    final FakeHttpLayer layer = new FakeHttpLayer();
    @Before
    public void setup() {
        api = new ApiWrapper("invalid", "invalid", URI.create("redirect://me"), null, CloudAPI.Env.SANDBOX) {
            @Override
            protected RequestDirector getRequestDirector(HttpRequestExecutor requestExec,
                                                         ClientConnectionManager conman,
                                                         ConnectionReuseStrategy reustrat,
                                                         ConnectionKeepAliveStrategy kastrat,
                                                         HttpRoutePlanner rouplan,
                                                         HttpProcessor httpProcessor,
                                                         HttpRequestRetryHandler retryHandler,
                                                         RedirectHandler redirectHandler,
                                                         AuthenticationHandler targetAuthHandler,
                                                         AuthenticationHandler proxyAuthHandler,
                                                         UserTokenHandler stateHandler,
                                                         HttpParams params) {
                return new RequestDirector() {
                    @Override
                    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
                            throws HttpException, IOException {
                        return layer.emulateRequest(target, request, context, this);
                    }
                };
            }
        };
        layer.clearHttpResponseRules();
    }

    @Test(expected = IllegalArgumentException.class)
    public void loginShouldThrowIllegalArgumentException() throws Exception {
        api.login(null, null);
    }

    @Test
    public void signupToken() throws Exception {
        layer.addPendingHttpResponse(200, "{\n" +
                "  \"access_token\":  \"04u7h-4cc355-70k3n\",\n" +
                "  \"expires_in\":    3600,\n" +
                "  \"scope\":         \"signup\",\n" +
                "  \"refresh_token\": \"04u7h-r3fr35h-70k3n\"\n" +
                "}");

        Token t = api.signupToken();
        assertThat(t.access, equalTo("04u7h-4cc355-70k3n"));
        assertThat(t.refresh, equalTo("04u7h-r3fr35h-70k3n"));
        assertThat(t.scope, equalTo("signup"));
        assertTrue(t.signupScoped());
        assertNotNull(t.getExpiresIn());
    }

    @Test
    public void exchangeOAuth1Token() throws Exception {
        layer.addPendingHttpResponse(200, "{\n" +
                "  \"access_token\":  \"04u7h-4cc355-70k3n\",\n" +
                "  \"expires_in\":    3600,\n" +
                "  \"scope\":         \"*\",\n" +
                "  \"refresh_token\": \"04u7h-r3fr35h-70k3n\"\n" +
                "}");
        api.exchangeToken("oldtoken");
    }

    @Test(expected = IllegalArgumentException.class)
    public void exchangeOAuth1TokenWithEmptyTokenShouldThrow() throws Exception {
        api.exchangeToken(null);
    }


    @Test
    public void shouldGetTokensWhenLoggingIn() throws Exception {
        layer.addPendingHttpResponse(200, "{\n" +
                "  \"access_token\":  \"04u7h-4cc355-70k3n\",\n" +
                "  \"expires_in\":    3600,\n" +
                "  \"scope\":         \"*\",\n" +
                "  \"refresh_token\": \"04u7h-r3fr35h-70k3n\"\n" +
                "}");

        Token t = api.login("foo", "bar");

        assertThat(t.access, equalTo("04u7h-4cc355-70k3n"));
        assertThat(t.refresh, equalTo("04u7h-r3fr35h-70k3n"));
        assertThat(t.scope, equalTo("*"));
        assertNotNull(t.getExpiresIn());
    }

    @Test
    public void shouldGetTokensWhenLoggingInViaAuthorizationCode() throws Exception {
        layer.addPendingHttpResponse(200, "{\n" +
                "  \"access_token\":  \"04u7h-4cc355-70k3n\",\n" +
                "  \"expires_in\":    3600,\n" +
                "  \"scope\":         \"*\",\n" +
                "  \"refresh_token\": \"04u7h-r3fr35h-70k3n\"\n" +
                "}");

        Token t = api.authorizationCode("code");

        assertThat(t.access, equalTo("04u7h-4cc355-70k3n"));
        assertThat(t.refresh, equalTo("04u7h-r3fr35h-70k3n"));
        assertThat(t.scope, equalTo("*"));
        assertNotNull(t.getExpiresIn());
    }

    @Test(expected = IOException.class)
    public void shouldThrowIOExceptionWhenLoginFailed() throws Exception {
        layer.addPendingHttpResponse(401, "{\n" +
                "  \"error\":  \"Error!\"\n" +
                "}");
        api.login("foo", "bar");
    }


    @Test(expected = IOException.class)
    public void shouldThrowIOExceptonWhenInvalidJSONReturned() throws Exception {
        layer.addPendingHttpResponse(200, "I'm invalid JSON!");
        api.login("foo", "bar");
    }

    @Test
    public void shouldContainInvalidJSONInExceptionMessage() throws Exception {
        layer.addPendingHttpResponse(200, "I'm invalid JSON!");
        try {
            api.login("foo", "bar");
            fail("expected IOException");
        } catch (IOException e) {
            assertThat(e.getMessage(), containsString("I'm invalid JSON!"));
        }
    }

    @Test
    public void shouldRefreshToken() throws Exception {
        layer.addPendingHttpResponse(200, "{\n" +
                "  \"access_token\":  \"fr3sh\",\n" +
                "  \"expires_in\":    3600,\n" +
                "  \"scope\":         null,\n" +
                "  \"refresh_token\": \"refresh\"\n" +
                "}");


        api.setToken(new Token("access", "refresh"));
        assertThat(api
                .refreshToken()
                .access,
                equalTo("fr3sh"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenNoRefreshToken() throws Exception {
        api.refreshToken();
    }

    @Test
    public void shouldResolveUris() throws Exception {
        HttpResponse r = mock(HttpResponse.class);
        StatusLine line = mock(StatusLine.class);
        when(line.getStatusCode()).thenReturn(302);
        when(r.getStatusLine()).thenReturn(line);
        Header location = mock(Header.class);
        when(location.getValue()).thenReturn("http://api.soundcloud.com/users/1000");
        when(r.getFirstHeader(anyString())).thenReturn(location);
        layer.addHttpResponseRule(new RequestMatcher() {
            @Override
            public boolean matches(HttpRequest request) {
                return true;
            }
        }, r);
        assertThat(api.resolve("http://soundcloud.com/crazybob"), is(1000L));
    }

    @Test
    public void resolveShouldReturnNegativeOneWhenInvalid() throws Exception {
        layer.addPendingHttpResponse(404, "Not found");
        assertThat(api.resolve("http://soundcloud.com/nonexisto"), equalTo(-1L));
    }

    @Test
    public void shouldGetContent() throws Exception {
        layer.addHttpResponseRule("/some/resource?a=1", "response");
        assertThat(Http.getString(api.getContent("/some/resource", new Params("a", "1"))),
                equalTo("response"));
    }

    @Test
    public void shouldPostContent() throws Exception {
        HttpResponse resp = mock(HttpResponse.class);
        layer.addHttpResponseRule("POST", "/foo/something?a=1", resp);
        assertThat(api.postContent("/foo/something", new Params("a", 1)),
                equalTo(resp));
    }

    @Test
    public void shouldPutContent() throws Exception {
        HttpResponse resp = mock(HttpResponse.class);
        layer.addHttpResponseRule("PUT", "/foo/something?a=1", resp);
        assertThat(api.putContent("/foo/something", new Params("a", 1)),
                equalTo(resp));
    }

    @Test
    public void shouldDeleteContent() throws Exception {
        HttpResponse resp = mock(HttpResponse.class);
        layer.addHttpResponseRule("DELETE", "/foo/something", resp);
        assertThat(api.deleteContent("/foo/something"), equalTo(resp));
    }

    @Test
    public void testGetOAuthHeader() throws Exception {
        Header h = ApiWrapper.getOAuthHeader(new Token("foo", "refresh"));
        assertThat(h.getName(), equalTo("Authorization"));
        assertThat(h.getValue(), equalTo("OAuth foo"));
    }

    @Test
    public void testGetOAuthHeaderNullToken() throws Exception {
        Header h = ApiWrapper.getOAuthHeader(null);
        assertThat(h.getName(), equalTo("Authorization"));
        assertThat(h.getValue(), equalTo("OAuth invalidated"));
    }


    @Test
    public void shouldGenerateUrlWithoutParameters() throws Exception {
        assertThat(
                api.getURI("/my-resource", null, true).toString(),
                equalTo("https://api.sandbox-soundcloud.com/my-resource")
        );
    }

    @Test
    public void shouldGenerateUrlWithoutSSL() throws Exception {
        assertThat(
                api.getURI("/my-resource", null, false).toString(),
                equalTo("http://api.sandbox-soundcloud.com/my-resource")
        );
    }

    @Test
    public void shouldGenerateUrlWithParameters() throws Exception {
        assertThat(
                api.getURI("/my-resource", new Params("foo", "bar"), true).toString(),
                equalTo("https://api.sandbox-soundcloud.com/my-resource?foo=bar")
        );
    }

    @Test
    public void shouldGenerateURIForLoginViaFacebook() throws Exception {
        assertThat(
                api.loginViaFacebook().toString(),
                        equalTo("https://api.sandbox-soundcloud.com/connect/via/facebook?redirect_uri=redirect%3A%2F%2Fme&client_id=invalid&response_type=code")
                );
    }
}
