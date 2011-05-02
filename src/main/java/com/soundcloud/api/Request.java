package com.soundcloud.api;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for constructing HTTP requests.
 */
public class Request implements Iterable<NameValuePair> {
    private List<NameValuePair> params = new ArrayList<NameValuePair>(); // XXX should probably be lazy
    private Map<String,File> files;
    private Token mToken;
    private String mResource;
    private TransferProgressListener listener;

    /** Empty request */
    public Request() {}

    /**
     * @param resource the base resource
     */
    public Request(String resource) {
       mResource = resource;
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
        params.add(new BasicNameValuePair(name, String.valueOf(value)));
        return this;
    }

    /**
     * @param args a list of arguments
     */
    public Request with(Object... args) {
       if (args != null) {
            if (args.length % 2 != 0) throw new IllegalArgumentException("need even number of arguments");
            for (int i = 0; i < args.length; i += 2) {
                this.add(args[i].toString(), args[i + 1]);
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
        return params.size();
    }

    /**
     * @return a String that is suitable for use as an <code>application/x-www-form-urlencoded</code>
     * list of parameters in an HTTP PUT or HTTP POST.
     */
    public String queryString() {
        return URLEncodedUtils.format(params, "UTF-8");
    }

    /** @return an URL with the query string parameters appended */
    public String toUrl(String resource) {
        return params.isEmpty() ? resource : resource + "?" + queryString();
    }

    public String toUrl() {
        return toUrl(mResource);
    }

    /**
     * Registers a file to be uploaded with a POST or PUT request.
     * @param name  the name of the file
     * @param file  the file to be submitted
     * @return this
     */
    public Request withFile(String name, File file) {
        if (files == null) files = new HashMap<String,File>();
        if (file != null)  files.put(name, file);
        return this;
    }


    /** Registers a listener for receiving notifications about transfer progress */
    public Request setProgressListener(TransferProgressListener listener) {
        this.listener = listener;
        return this;
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
                // multipart ?
                if (files != null && !files.isEmpty()) {
                    MultipartEntity multiPart = new MultipartEntity();
                    for (Map.Entry<String,File> e : files.entrySet()) {
                        multiPart.addPart(e.getKey(), new FileBody(e.getValue()));
                    }
                    for (NameValuePair pair : params) {
                        multiPart.addPart(pair.getName(), new StringBodyNoHeaders(pair.getValue()));
                    }
                    enclosingRequest.setEntity(listener == null ? multiPart :
                        new CountingMultipartEntity(multiPart, listener));
                // form-urlencoded?
                } else if (!params.isEmpty()) {
                    request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    enclosingRequest.setEntity(new StringEntity(queryString()));
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

    @Override public String toString() {
        return mResource == null ? queryString() : toUrl();
    }

    @Override public Iterator<NameValuePair> iterator() {
        return params.iterator();
    }

    /**
     * Updates about the amount of bytes already transferred.
     */
    public static interface TransferProgressListener {
        /**
         * @param amount number of bytes already transferred.
         */
        public void transferred(long amount);
    }

}
