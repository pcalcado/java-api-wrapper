package com.soundcloud.api.fakehttp;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FakeHttpResponse extends HttpResponseStub {

    private int statusCode;
    private String responseBody;
    private Header contentType;
    private TestStatusLine statusLine = new TestStatusLine();
    private TestHttpEntity httpEntity = new TestHttpEntity();

    public FakeHttpResponse(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public FakeHttpResponse(int statusCode, String responseBody, Header contentType) {
        this(statusCode, responseBody);
        this.contentType = contentType;
    }

    @Override public StatusLine getStatusLine() {
        return statusLine;
    }

    @Override public HttpEntity getEntity() {
        return httpEntity;
    }

    public class TestHttpEntity extends HttpEntityStub {
        @Override public long getContentLength() {
            return responseBody.length();
        }

        @Override public Header getContentType() {
            return contentType;
        }

        @Override public boolean isStreaming() {
            return true;
        }

        @Override public boolean isRepeatable() {
            return true;
        }

        @Override public InputStream getContent() throws IOException, IllegalStateException {
            return new ByteArrayInputStream(responseBody.getBytes());
        }

        @Override public void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(responseBody.getBytes());
        }

        @Override public void consumeContent() throws IOException {
        }
    }

    public class TestStatusLine extends StatusLineStub {
        @Override public ProtocolVersion getProtocolVersion() {
            return new HttpVersion(1, 0);
        }

        @Override public int getStatusCode() {
            return statusCode;
        }

        @Override public String getReasonPhrase() {
            return "HTTP status " + statusCode;
        }
    }
}
