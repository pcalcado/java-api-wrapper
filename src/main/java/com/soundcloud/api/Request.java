package com.soundcloud.api;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for constructing HTTP requests.
 *
 * Example:
 * <code>
 *   <pre>
 *  HttpRequest request = Request.to("/tracks")
 *     .with("track[user]", 1234)
 *     .withFile("track[asset_data]", new File("track.mp3")
 *     .buildRequest(HttpPost.class);
 *
 *  httpClient.execute(request);
 *   </pre>
 *  </code>
 */
public class Request implements Iterable<NameValuePair> {
    private List<NameValuePair> mParams = new ArrayList<NameValuePair>(); // XXX should probably be lazy
    private Map<String, File> mFiles;
    private Map<String, ByteBuffer> mByteBuffers;
    private HttpEntity mEntity;

    private Token mToken;
    private String mResource;
    private TransferProgressListener listener;

    /** Empty request */
    public Request() {}

    /**
     * @param resource the base resource
     */
    public Request(String resource) {
        if (resource != null && resource.contains("?")) {
            String query = resource.substring(Math.min(resource.length(), resource.indexOf("?")+1),
                    resource.length());
            for (String s : query.split("&")) {
                String[] kv = s.split("=", 2);
                if (kv != null && kv.length == 2) {
                    try {
                        mParams.add(new BasicNameValuePair(
                                URLDecoder.decode(kv[0], "UTF-8"),
                                URLDecoder.decode(kv[1], "UTF-8")));
                    } catch (UnsupportedEncodingException ignored) {}
                }
            }
            mResource = resource.substring(0, resource.indexOf("?"));
        } else {
            mResource = resource;
        }
    }

    /**
     * @param request the request to be copied
     */
    public Request(Request request) {
        mResource = request.mResource;
        mToken = request.mToken;
        listener = request.listener;
        mParams = new ArrayList<NameValuePair>(request.mParams);
        if (request.mFiles != null) mFiles = new HashMap<String, File>(request.mFiles);
    }

    /**
     * @param resource  the resource to request
     * @param args      optional string expansion arguments (passed to String#format(String, Object...)
     * @throws java.util.IllegalFormatException - If a format string contains an illegal syntax,
     * @return the request
     * @see String#format(String, Object...)
     */
    public static Request to(String resource, Object... args) {
        if (args != null &&
            args.length > 0) {
            resource = String.format(resource, args);
        }
        return new Request(resource);
    }

    /**
     * Adds a key value pair
     * @param name  the name
     * @param value the value
     * @return this
     */
    public Request add(String name, Object value) {
        mParams.add(new BasicNameValuePair(name, String.valueOf(value)));
        return this;
    }

    /**
     * @param args a list of arguments
     * @return this
     */
    public Request with(Object... args) {
       if (args != null) {
            if (args.length % 2 != 0) throw new IllegalArgumentException("need even number of arguments");
            for (int i = 0; i < args.length; i += 2) {
                add(args[i].toString(), args[i + 1]);
            }
       }
       return this;
    }

    /**
     * The request should be made with a specific token.
     * @param token the token
     * @return this
     */
    public Request usingToken(Token token) {
        mToken = token;
        return this;
    }

    /** @return the size of the parameters */
    public int size() {
        return mParams.size();
    }

    /**
     * @return a String that is suitable for use as an <code>application/x-www-form-urlencoded</code>
     * list of parameters in an HTTP PUT or HTTP POST.
     */
    public String queryString() {
        return URLEncodedUtils.format(mParams, "UTF-8");
    }

    /**
     * @param  resource the resource
     * @return an URL with the query string parameters appended
     */
    public String toUrl(String resource) {
        return mParams.isEmpty() ? resource : resource + "?" + queryString();
    }

    public String toUrl() {
        return toUrl(mResource);
    }

    /**
     * Registers a file to be uploaded with a POST or PUT request.
     * @param name  the name of the parameter
     * @param file  the file to be submitted
     * @return this
     */
    public Request withFile(String name, File file) {
        if (mFiles == null) mFiles = new HashMap<String,File>();
        if (file != null)  mFiles.put(name, file);
        return this;
    }

    /**
     * Registers binary data to be uploaded with a POST or PUT request.
     * @param name  the name of the parameter
     * @param data  the data to be submitted
     * @return this
     */
    public Request withFile(String name, byte[] data) {
        return withFile(name, ByteBuffer.wrap(data));
    }

    /**
     * Registers binary data to be uploaded with a POST or PUT request.
     * @param name  the name of the parameter
     * @param data  the data to be submitted
     * @return this
     */
    public Request withFile(String name, ByteBuffer data) {
        if (mByteBuffers == null) mByteBuffers = new HashMap<String, ByteBuffer>();
        if (data != null) mByteBuffers.put(name, data);
        return this;
    }

    /**
     * Adds an arbitrary entity to the request (used with POST/PUT)
     * @param entity the entity to POST/PUT
     * @return this
     */
    public Request withEntity(HttpEntity entity) {
        mEntity = entity;
        return this;
    }

    /**
     * Adds string content to the request (used with POST/PUT)
     * @param content the content to POST/PUT
     * @param contentType the content type
     * @return this
     */
    public Request withContent(String content, String contentType) {
        try {
            StringEntity stringEntity = new StringEntity(content);
            if (contentType != null) {
                stringEntity.setContentType(contentType);
            }
            return withEntity(stringEntity);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param listener a listener for receiving notifications about transfer progress
     * @return this
     */
    public Request setProgressListener(TransferProgressListener listener) {
        this.listener = listener;
        return this;
    }

    public boolean isMultipart() {
        return (mFiles != null && !mFiles.isEmpty()) ||
               (mByteBuffers != null && !mByteBuffers.isEmpty());
    }

    /**
     * Builds a request with the given set of parameters and files.
     * @param method    the type of request to use
     * @param <T>       the type of request to use
     * @return HTTP request, prepared to be executed
     */
    public <T extends HttpRequestBase> T buildRequest(Class<T> method) {
        try {
            T request = method.newInstance();
            // POST/PUT ?
            if (request instanceof HttpEntityEnclosingRequestBase) {
                HttpEntityEnclosingRequestBase enclosingRequest =
                        (HttpEntityEnclosingRequestBase) request;

                if (isMultipart()) {
                    MultipartEntity multiPart = new MultipartEntity();

                    if (mFiles != null) {
                        for (Map.Entry<String,File> e : mFiles.entrySet()) {
                            multiPart.addPart(e.getKey(), new FileBody(e.getValue()));
                        }
                    }

                    if (mByteBuffers != null) {
                        for (Map.Entry<String, ByteBuffer> e : mByteBuffers.entrySet()) {
                            multiPart.addPart(e.getKey(), new ByteBufferBody(e.getValue()));
                        }
                    }

                    for (NameValuePair pair : mParams) {
                        multiPart.addPart(pair.getName(), new StringBodyNoHeaders(pair.getValue()));
                    }

                    enclosingRequest.setEntity(listener == null ? multiPart :
                        new CountingMultipartEntity(multiPart, listener));
                // form-urlencoded?
                } else if (!mParams.isEmpty()) {
                    request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    enclosingRequest.setEntity(new StringEntity(queryString()));
                } else if (mEntity != null) {
                    request.setHeader(mEntity.getContentType());
                    enclosingRequest.setEntity(mEntity);
                }

                request.setURI(URI.create(mResource));
            } else { // just plain GET/DELETE/...
                request.setURI(URI.create(toUrl()));
            }

            if (mToken != null) {
                request.addHeader(ApiWrapper.createOAuthHeader(mToken));
            }
            return request;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            // XXX really rethrow?
            throw new RuntimeException(e);
        }
    }

    @Override public Iterator<NameValuePair> iterator() {
        return mParams.iterator();
    }

    @Override
    public String toString() {
        return "Request{" +
                "params=" + mParams +
                ", files=" + mFiles +
                ", entity=" + mEntity +
                ", mToken=" + mToken +
                ", mResource='" + mResource + '\'' +
                ", listener=" + listener +
                '}';
    }

    /* package */ Token getToken() {
        return mToken;
    }

    /* package */ TransferProgressListener getListener() {
        return listener;
    }


    /**
     * Updates about the amount of bytes already transferred.
     */
    public static interface TransferProgressListener {
        /**
         * @param amount number of bytes already transferred.
         * @throws IOException if the transfer should be cancelled
         */
        public void transferred(long amount) throws IOException;
    }



    static class StringBodyNoHeaders extends StringBody {
        public StringBodyNoHeaders(String value) throws UnsupportedEncodingException {
            super(value);
        }

        @Override public String getMimeType() {
            return null;
        }

        @Override public String getTransferEncoding() {
            return null;
        }
    }

    static class ByteBufferBody extends AbstractContentBody {
        private ByteBuffer mBuffer;

        public ByteBufferBody(ByteBuffer buffer) {
            super("application/octet-stream");
            mBuffer = buffer;
        }

        @Override
        public String getFilename() {
            return null;
        }

        public String getTransferEncoding() {
            return MIME.ENC_BINARY;
        }

        public String getCharset() {
            return null;
        }

        @Override
        public long getContentLength() {
            return mBuffer.capacity();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            if (mBuffer.hasArray()) {
                out.write(mBuffer.array());
            } else {
                byte[] dst = new byte[mBuffer.capacity()];
                mBuffer.get(dst);
                out.write(dst);
            }
        }
    }
}
