package com.soundcloud.api;

import static org.mockito.Mockito.*;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthState;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public class OAuthHttpRequestInterceptorTests {
    OAuthHttpRequestInterceptor interceptor;
    HttpRequest request;
    HttpContext context;
    AuthState authState;

    @Before
    public void setup() {
        interceptor = new OAuthHttpRequestInterceptor();
        request = mock(HttpRequest.class);
        RequestLine line = mock(RequestLine.class);
        when(line.getMethod()).thenReturn("GET");
        when(request.getRequestLine()).thenReturn(line);
        context = new BasicHttpContext();
        authState = new AuthState();

        context.setAttribute(ClientContext.TARGET_AUTH_STATE, authState);
    }

    @Test
    public void shouldCallAuthenticate() throws Exception {
        AuthScheme scheme = mock(AuthScheme.class);
        authState.setAuthScheme(scheme);
        interceptor.process(request, context);

        verify(scheme).authenticate(
                Matchers.<org.apache.http.auth.Credentials>anyObject(),
                Matchers.<HttpRequest>anyObject());

        verify(request).setHeader(Matchers.<Header>anyObject());
    }
}
