package com.soundcloud.api;

import org.apache.http.entity.mime.content.StringBody;

import java.io.UnsupportedEncodingException;

class StringBodyNoHeaders extends StringBody {
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
