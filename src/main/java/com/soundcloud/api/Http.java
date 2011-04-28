package com.soundcloud.api;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;

public class Http {
    public static final int BUFFER_SIZE = 8192;
    public static final int TIMEOUT = 20 * 1000;

    private Http() {}

    public static String getString(HttpResponse response) throws IOException {
        InputStream is = response.getEntity().getContent();
        if (is == null) return null;

        int length = BUFFER_SIZE;
        Header contentLength = null;
        try {
            contentLength = response.getFirstHeader(HTTP.CONTENT_LEN);
        } catch (UnsupportedOperationException ignored) {
        }

        if (contentLength != null) {
            try {
                length = Integer.parseInt(contentLength.getValue());
            } catch (NumberFormatException ignored) {
            }
        }

        final StringBuilder sb = new StringBuilder(length);
        int n;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((n = is.read(buffer)) != -1) sb.append(new String(buffer, 0, n));
        return sb.toString();
    }


    /**
     * @see android.net.http.AndroidHttpClient#newInstance(String, Context)
     * @return the default HttpParams
     */
    public static HttpParams defaultParams(){
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // Turn off stale checking.  Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        // fix contributed by Bjorn Roche (XXX check if still needed)
        params.setBooleanParameter("http.protocol.expect-continue", false);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRoute() {
            @Override
            public int getMaxForRoute(HttpRoute httpRoute) {
                return ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE * 3;
            }
        });
        return params;
    }
}
