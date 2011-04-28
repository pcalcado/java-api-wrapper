package com.soundcloud.api;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

class CountingMultipartEntity implements HttpEntity {
    private HttpEntity mDelegate;
    private Params.ProgressListener mListener;

    public CountingMultipartEntity(HttpEntity delegate, Params.ProgressListener listener) {
        super();
        mDelegate = delegate;
        mListener = listener;
    }

    public void consumeContent() throws IOException {
        mDelegate.consumeContent();
    }

    public InputStream getContent() throws IOException, IllegalStateException {
        return mDelegate.getContent();
    }

    public Header getContentEncoding() {
        return mDelegate.getContentEncoding();
    }

    public long getContentLength() {
        return mDelegate.getContentLength();
    }

    public Header getContentType() {
        return mDelegate.getContentType();
    }

    public boolean isChunked() {
        return mDelegate.isChunked();
    }

    public boolean isRepeatable() {
        return mDelegate.isRepeatable();
    }

    public boolean isStreaming() {
        return mDelegate.isStreaming();
    }

    public void writeTo(OutputStream outstream) throws IOException {
        mDelegate.writeTo(new CountingOutputStream(outstream, mListener));
    }

    private static class CountingOutputStream extends FilterOutputStream {
        private final Params.ProgressListener mListener;
        private long mTransferred;

        public CountingOutputStream(final OutputStream out, final Params.ProgressListener listener) {
            super(out);
            this.mListener = listener;
            this.mTransferred = 0;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.mTransferred += len;
            if (mListener != null) this.mListener.transferred(this.mTransferred);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            this.mTransferred++;
            if (mListener != null) this.mListener.transferred(this.mTransferred);
        }
    }
}
