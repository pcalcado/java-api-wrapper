package com.soundcloud.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;


public class TokenTest {
    @Test
    public void shouldInvalidateToken() throws Exception {
        Token t = new Token("1", "2");
        t.invalidate();
        assertNull(t.access);
        assertFalse(t.valid());
    }

    @Test
    public void shouldBeValidWhenAccessAndRefreshTokenArePresent() throws Exception {
        Token t = new Token("1", "2");
        assertTrue(t.valid());
    }

    @Test
    public void shouldBeValidWhenOnlyAccessTokenPresentAndNonExpirationScoped() throws Exception {
        Token t = new Token("1", null, Token.SCOPE_NON_EXPIRING);
        assertTrue(t.valid());
    }

    @Test
    public void emptyTokenShouldBeInValid() throws Exception {
        Token invalid = new Token(null, "2");
        assertFalse(invalid.valid());
    }

    @Test
    public void shouldDefaultScope() throws Exception {
        Token t = new Token(null, null);
        assertFalse(t.defaultScoped());

        t = new Token(null, null, "*");
        assertTrue(t.defaultScoped());
    }

    @Test
    public void shouldDetectSignupScope() throws Exception {
        assertFalse(new Token(null, null).signupScoped());
        assertTrue(new Token(null, null, "signup").signupScoped());
    }

    @Test
    public void shouldProperlySeparateTokens() throws Exception {
        Token t = new Token(null, null);
        assertFalse(t.signupScoped());

        t = new Token(null, null, "notreallysignup");
        assertFalse(t.signupScoped());
    }

    @Test
    public void shouldHaveProperEqualsMethod() throws Exception {
        assertEquals(new Token("1", "2"), new Token("1", "2"));
        assertFalse(new Token("1", "2").equals(new Token("1", "3")));
        assertFalse(new Token("1", "2").equals(new Token("1", "2", "bla")));
        assertEquals(new Token("1", "2", "bla"), new Token("1", "2", "bla"));
    }

    @Test
    public void shouldParseJsonResponse() throws Exception {
        Token t = new Token(new JSONObject("{\n" +
                "    \"access_token\": \"1234\",\n" +
                "    \"refresh_token\": \"5678\",\n" +
                "    \"expires_in\":   3600,\n" +
                "    \"scope\":    \"*\"\n" +
                "}"));

        assertThat(t.scoped("*"), is(true));
        assertThat(t.access, equalTo("1234"));
        assertThat(t.refresh, equalTo("5678"));
        assertNotNull(t.getExpiresIn());
    }
}
