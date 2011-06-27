package com.soundcloud.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Represents an OAuth2 access/refresh token pair.
 */
public class Token implements Serializable {
    private static final long serialVersionUID = 766168501082045382L;

    public static final String ACCESS_TOKEN  = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String SCOPE         = "scope";
    public static final String EXPIRES_IN    = "expires_in";

    public static final String SCOPE_DEFAULT      = "*";

    /** Special scope for signup / password recovery */
    public static final String SCOPE_SIGNUP       = "signup";
    public static final String SCOPE_PLAYCOUNT    = "playcount";

    /** Don't expire access token - returned tokens won't include a refresh token */
    public static final String SCOPE_NON_EXPIRING = "non-expiring";

    // XXX these should be private
    public String access, refresh, scope;
    public long expiresIn;

    /**
     * Constructs a new token with the given sub-tokens
     * @param access   A token used by the client to make authenticated requests on behalf of the resource owner.
     * @param refresh  A token used by the client to obtain a new access token without having
     * to involve the resource owner.
     */
    public Token(String access, String refresh) {
        this(access, refresh, null);
    }

    public Token(String access, String refresh, String scope) {
        this.access = access;
        this.refresh = refresh;
        this.scope = scope;
    }

    /**
     * Construct a new token from a JSON response
     * @param json the json response
     * @throws IOException JSON format error
     */
    public Token(JSONObject json) throws IOException {
        try {
            access = json.getString(ACCESS_TOKEN);
            if (json.has(REFRESH_TOKEN)) {
                // refresh token won't be set if we don't expire
                refresh = json.getString(REFRESH_TOKEN);
                expiresIn = System.currentTimeMillis() + json.getLong(EXPIRES_IN) * 1000;
            }
            scope = json.getString(SCOPE);
        } catch (JSONException e) {
            throw new IOException(e.getMessage());
        }
    }

    /** Invalidates the access token */
    public void invalidate() {
        this.access = null;
    }

    /**
     * @return null or the date of expiration of this token
     */
    public Date getExpiresIn() {
        return expiresIn == 0 ? null : new Date(expiresIn);
    }

    public boolean defaultScoped() {
        return scoped(SCOPE_DEFAULT);
    }

    /** @return has token the signup scope ("signup") */
    public boolean signupScoped() {
        return scoped(SCOPE_SIGNUP);
    }

    public boolean scoped(String scope) {
        if (this.scope != null) {
            for (String s : this.scope.split(" "))
                if (scope.equals(s)) return true;
        }
        return false;
    }

    /** @return is this token valid */
    public boolean valid() {
        return access != null && (scoped(SCOPE_NON_EXPIRING) || refresh != null);
    }

    @Override
    public String toString() {
        return "Token{" +
                "access='" + access + '\'' +
                ", refresh='" + refresh + '\'' +
                ", scope='" + scope + '\'' +
                ", expires=" + getExpiresIn() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof Token) {
            Token token = (Token) o;
            if (access != null ? !access.equals(token.access) : token.access != null) return false;
            if (refresh != null ? !refresh.equals(token.refresh) : token.refresh != null) return false;
            if (scope != null ? !scope.equals(token.scope) : token.scope != null) return false;
            return true;
        } else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        int result = access != null ? access.hashCode() : 0;
        result = 31 * result + (refresh != null ? refresh.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        return result;
    }
}
