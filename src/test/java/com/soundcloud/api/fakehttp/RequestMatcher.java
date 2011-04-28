package com.soundcloud.api.fakehttp;

import org.apache.http.HttpRequest;

public interface RequestMatcher {
    public boolean matches(HttpRequest request);
}
