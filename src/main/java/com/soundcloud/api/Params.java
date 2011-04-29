package com.soundcloud.api;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
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
 * Convenience class for construction HTTP requests.
 */
public class Params implements Iterable<NameValuePair> {
    Token token;
    Map<String,File> files;
    public final List<NameValuePair> params = new ArrayList<NameValuePair>();
    private TransferProgressListener listener;

    /**
     * <code>new Params("page", 10, offset", 0).queryString() // page=10&offset=0
     * @param args a list of arguments
     */
    public Params(Object... args) {
        if (args != null) {
            if (args.length % 2 != 0) throw new IllegalArgumentException("need even number of arguments");
            for (int i = 0; i < args.length; i += 2) {
                this.add(args[i].toString(), args[i + 1]);
            }
        }
    }

    /**
     * Adds a key value pair
     * @param name  the name
     * @param value the value
     * @return this
     */
    public Params add(String name, Object value) {
        params.add(new BasicNameValuePair(name, String.valueOf(value)));
        return this;
    }

    /**
     * The request should be made with a specific token.
     * @param t the token
     * @return this
     */
    public Params withToken(Token t) {
        token = t;
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
    public String url(String url) {
        return params.isEmpty() ? url : url + "?" + queryString();
    }

    /**
     * Registers a file to be uploaded with a POST or PUT request.
     * @param name  the name of the file
     * @param file  the file to be submitted
     * @return this
     */
    public Params addFile(String name, File file) {
        if (files == null) files = new HashMap<String,File>();
        if (file != null)  files.put(name, file);
        return this;
    }


    /** Registers a listener for receiving notifications about transfer progress */
    public Params setProgressListener(TransferProgressListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Builds a request with the given set of parameters and files.
     * @param method    the type of request to use
     * @param resource  the resource to access
     * @param <T>       the type of request to use
     * @return HTTP request, prepared to be executed
     */
    public <T extends HttpRequestBase> T buildRequest(Class<T> method, String resource) {
        try {
            HttpRequestBase request = method.newInstance();
            if (token != null) {
                request.addHeader(ApiWrapper.createOAuthHeader(token));
            }

            if (files != null && !files.isEmpty() && request instanceof HttpEntityEnclosingRequestBase) {
                MultipartEntity entity = new MultipartEntity();
                for (Map.Entry<String,File> e : files.entrySet()) {
                    entity.addPart(e.getKey(), new FileBody(e.getValue()));
                }
                for (NameValuePair pair : params) {
                    try {
                        entity.addPart(pair.getName(), new StringBodyNoHeaders(pair.getValue()));
                    } catch (UnsupportedEncodingException ignored) {
                    }
                }

                ((HttpEntityEnclosingRequestBase)request).setEntity(listener == null ? entity :
                    new CountingMultipartEntity(entity, listener));

                request.setURI(URI.create(resource));
            } else {
                request.setURI(URI.create(url(resource)));
            }
            //noinspection unchecked
            return (T) request;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public String toString() {
        return queryString();
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

    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.2-Resources%3A-tracks">Tracks</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static interface Track {
        String TITLE         = "track[title]";          // required
        String TYPE          = "track[track_type]";
        String ASSET_DATA    = "track[asset_data]";
        String ARTWORK_DATA  = "track[artwork_data]";
        String POST_TO       = "track[post_to][][id]";
        String POST_TO_EMPTY = "track[post_to][]";
        String TAG_LIST      = "track[tag_list]";
        String SHARING       = "track[sharing]";
        String STREAMABLE    = "track[streamable]";
        String DOWNLOADABLE  = "track[downloadable]";
        String SHARED_EMAILS = "track[shared_to][emails][][address]";
        String SHARING_NOTE  = "track[sharing_note]";
        String PUBLIC        = "public";
        String PRIVATE       = "private";
    }

    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.1-Resources%3A-users">Users</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static interface User {
        String NAME                  = "user[username]";
        String PERMALINK             = "user[permalink]";
        String EMAIL                 = "user[email]";
        String PASSWORD              = "user[password]";
        String PASSWORD_CONFIRMATION = "user[password_confirmation]";
        String TERMS_OF_USE          = "user[terms_of_use]";
        String AVATAR                = "user[avatar_data]";
    }

    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.5-Resources%3A-comments">Comments</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static interface Comment {
        String BODY      = "comment[body]";
        String TIMESTAMP = "comment[timestamp]";
        String REPLY_TO  = "comment[reply_to]";
    }
}
