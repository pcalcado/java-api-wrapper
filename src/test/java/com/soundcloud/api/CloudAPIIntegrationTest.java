package com.soundcloud.api;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CloudAPIIntegrationTest implements Params.Track, Endpoints {
    // http://sandbox-soundcloud.com/you/apps/java-api-wrapper-test-app
    static final String CLIENT_ID     = "yH1Jv2C5fhIbZfGTpKtujQ";
    static final String CLIENT_SECRET = "C6o8jc517b6PIw0RKtcfQsbOK3BjGpxWFLg977UiguY";

    CloudAPI api;

    /*
    To get full HTTP logging, add the following system properties:
    -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
    -Dorg.apache.commons.logging.simplelog.showdatetime=true
    -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG
    -Dorg.apache.commons.logging.simplelog.log.org.apache.http.wire=ERROR
    */

    @Before
    public void setUp() throws Exception {
        api = new ApiWrapper(
                CLIENT_ID,
                CLIENT_SECRET,
                null,
                null,
                Env.SANDBOX);
    }

    private Token login() throws IOException {
        return login(null);
    }

    private Token login(String scope) throws IOException {
        return api.login("api-testing", "testing", scope);
    }

    @Test
    public void shouldUploadASimpleAudioFile() throws Exception {
        login();
        HttpResponse resp = api.post(Request.to(TRACKS).with(
                  TITLE, "Hello Android",
                  POST_TO_EMPTY, "")
                .withFile(ASSET_DATA, new File(getClass().getResource("hello.aiff").getFile())));

        int status = resp.getStatusLine().getStatusCode();
        assertThat(status, is(201));
    }

    @Test(expected = IOException.class)
    public void shouldNotGetASignupTokenWhenInofficialApp() throws Exception {
        login();
        api.clientCredentials();
    }

    @Test
    public void shouldReturn401WithInvalidToken() throws Exception {
        login();
        api.setToken(new Token("invalid", "invalid"));
        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(401));
    }

    @Test
    public void shouldRefreshAutomaticallyWhenTokenExpired() throws Exception {
        login();

        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));

        final Token oldToken = api.getToken();

        assertThat(api.invalidateToken(), is(nullValue()));

        resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));
        // make sure we've got a new token
        assertThat(oldToken, not(equalTo(api.getToken())));
    }

    @Test
    public void shouldResolveUrls() throws Exception {
        login();

        long id = api.resolve("http://sandbox-soundcloud.com/api-testing");
        assertThat(id, is(1862213L));
    }

    @Test
    public void readMyDetails() throws Exception {
        login();

        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));

        JSONObject me = Http.getJSON(resp);

        assertThat(me.getString("username"), equalTo("api-testing"));
        // writeResponse(resp, "me.json");
    }

    @Test
    public void shouldLoginWithNonExpiringScope() throws Exception {
        Token token = login(Token.SCOPE_NON_EXPIRING);
        assertThat(token.scoped(Token.SCOPE_NON_EXPIRING), is(true));
        assertThat(token.refresh, is(nullValue()));
        assertThat(token.getExpiresIn(), is(nullValue()));
        assertThat(token.valid(), is(true));

        // make sure we can issue a request with this token
        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void shouldNotRefreshWithNonExpiringScope() throws Exception {
        Token token = login(Token.SCOPE_NON_EXPIRING);
        assertThat(token.scoped(Token.SCOPE_NON_EXPIRING), is(true));
        assertThat(api.invalidateToken(), is(nullValue()));
        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(401));
    }

    /*
    @Test
    public void updateMyDetails() throws Exception {
        Request updateMe = Request.to(MY_DETAILS).with(
                Params.User.WEBSITE, "http://mywebsite.com")
                .withFile(Params.User.AVATAR, new File(getClass().getResource("cat.jpg").getFile()));

        HttpResponse resp = api.put(updateMe);
        assertThat(resp.getStatusLine().getStatusCode(), is(200));
    }
    */

    @SuppressWarnings({"UnusedDeclaration"})
    private void writeResponse(HttpResponse resp, String file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        InputStream is = resp.getEntity().getContent();
        byte[] b = new byte[8192];
        int n;

        while ((n = is.read(b)) >= 0) fos.write(b, 0, n);
        is.close();
        fos.close();
    }
}
