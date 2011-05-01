package com.soundcloud.api;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AUTH;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RequestTests {
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentForNonEvenParams() throws Exception {
        new Request().with("1", 2, "3");
    }

    @Test
    public void shouldBuildAQueryString() throws Exception {
        assertThat(
                new Request().with("foo", 100, "baz", 22.3f, "met\u00f8l", false).toString(),
                equalTo("foo=100&baz=22.3&met%C3%B8l=false"));
    }

    @Test
    public void shouldHaveToStringAsQueryString() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        assertThat(p.queryString(), equalTo(p.toString()));
    }

    @Test
    public void shouldGenerateUrlWithParameters() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        assertThat(p.toUrl("http://foo.com"), equalTo("http://foo.com?foo=100&baz=22.3"));
    }

    @Test
    public void shouldHaveSizeMethod() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        assertThat(p.size(), is(2));
    }

    @Test
    public void shouldSupportWith() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        p.add("baz", 66);
        assertThat(p.size(), is(3));
        assertThat(p.queryString(), equalTo("foo=100&baz=22.3&baz=66"));
    }

    @Test
    public void shouldImplementIterable() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        Iterator<NameValuePair> it = p.iterator();
        assertThat(it.next().getName(), equalTo("foo"));
        assertThat(it.next().getName(), equalTo("baz"));
        try {
            it.next();
            throw new RuntimeException("NoSuchElementException expected");
        } catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void shouldGetStringFromHttpResponse() throws Exception {
        HttpResponse resp = mock(HttpResponse.class);
        HttpEntity ent = mock(HttpEntity.class);
        when(ent.getContent()).thenReturn(new ByteArrayInputStream("foo".getBytes()));
        when(resp.getEntity()).thenReturn(ent);

        assertThat(Http.getString(resp), equalTo("foo"));
    }

    @Test
    public void shouldBuildARequest() throws Exception {
        HttpGet request = Request.to("/foo").with("1", "2").buildRequest(HttpGet.class);
        assertThat(request.getURI().toString(), equalTo("/foo?1=2"));
    }

    @Test
    public void shouldAddTokenToHeaderIfSpecified() throws Exception {
        HttpGet request = Request.to("/foo")
                .with("1", "2")
                .usingToken(new Token("acc3ss", "r3fr3sh"))
                .buildRequest(HttpGet.class);

        Header auth = request.getFirstHeader(AUTH.WWW_AUTH_RESP);
        assertNotNull(auth);
        assertThat(auth.getValue(), CoreMatchers.containsString("acc3ss"));
    }

    @Test
    public void shouldCreateMultipartRequestWhenFilesAreAdded() throws Exception {
        File f = File.createTempFile("testing", "test");

        HttpPost request = Request.to("/foo")
                .with("key", "value")
                .withFile("foo", f)
                .buildRequest(HttpPost.class);

        assertTrue(request.getEntity() instanceof MultipartEntity);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.getEntity().writeTo(os);
        String encoded = os.toString();
        assertThat(encoded, containsString("foo"));
        assertThat(encoded, containsString("key"));
        assertThat(encoded, containsString("value"));
        assertThat(encoded, containsString("testing"));
    }

    @Test
    public void whenAProgressListenerIsSpecifiedShouldHaveCountingMultipart() throws Exception {
        HttpPost request = Request.to("/foo")
                .with("key", "value")
                .withFile("foo", new File("/tmp"))
                .setProgressListener(new Request.TransferProgressListener() {
                    @Override
                    public void transferred(long amount) {
                    }
                })
                .buildRequest(HttpPost.class);
        assertTrue(request.getEntity() instanceof CountingMultipartEntity);
    }

    @Test
    public void shouldDoStringFormattingInFactoryMethod() throws Exception {
        assertThat(Request.to("/resource/%d", 200).toUrl(), equalTo("/resource/200"));
    }

    @Test(expected = IllegalFormatException.class)
    public void shouldThrowIllegalFormatExceptionWhenInvalidParameters() throws Exception {
        Request.to("/resource/%d", "int").toUrl();
    }
}
