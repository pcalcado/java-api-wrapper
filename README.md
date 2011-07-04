# java-api-wrapper

OAuth2 SoundCloud API wrapper written in Java ([javadoc][]), extracted from the
[SoundCloud Android][] codebase.

It is simple to use and requires a minimum of external dependencies (compared to
the OAuth1 wrapper) so should be easily embeddable in both desktop and
mobile applications.

## Usage

Create a wrapper instance:

    ApiWrapper wrapper = new ApiWrapper("client_id", "client_secret",
                                        null, null, Env.LIVE);

Obtain a token:

    wrapper.login("username", "password");

Execute a request:

    HttpResponse resp = wrapper.get(Request.to("/me"));

Update a resource:

    HttpResponse resp =
          wrapper.put(Request.to("/me")
                 .with("user[full_name]", "Che Flute",
                       "user[website]",   "http://cheflute.com")
                 .withFile("user[avatar_data]", new File("flute.jpg")));

## Migrating from OAuth1

If your app uses OAuth1 and already has users with access tokens
you can easily migrate to OAuth2 without requiring anybody to reauthenticate:

    Token token = wrapper.exchangeOAuth1Token("validoauth1token");

Note that this is specific to SoundCloud and not part of the current OAuth2
draft.

## Refresh tokens

OAuth2 access tokens are only valid for a certain amount of time (usually 1h)
and need to be refreshed when they become stale. The wrapper automatically
refreshes the token and retries the request so an API client usually does not
need to care about this fact. If the client is interested (possibly to persist
the updated token) it can register a listener with the wrapper.

## Non-expiring access tokens (only applies to version 1.0.1+)

Expiring access tokens provide more security but also add more complexity to
the authentication process. If you don't want to use them you can request
non-expiring tokens by specifying the scope "non-expiring" when exchanging the
tokens:

    Token token = wrapper.login("username", "password", Token.SCOPE_NON_EXPIRING);

The resulting token will be valid until revoked manually.

For the `authorization_code` grant type you need to request the scope like so:

    URI uri = wrapper.authorizationCodeUrl(Endpoints.CONNECT, Token.SCOPE_NON_EXPIRING);
    // open uri in browser / WebView etc.

## Requirements

The wrapper depends on [Apache HttpClient][] (including the [HttpMime][]
module) and [json-java][]. The Android SDK already comes with these two
libraries so you don't need to include them when using the wrapper there.

## Build + Test

The project uses the groovy-based build system [gradle][] (version 1.x):

    $ brew update && brew install gradle (OSX+homebrew, check website for other OS)
    $ git clone git://github.com/soundcloud/java-api-wrapper.git
    $ cd java-api-wrapper
    $ gradle jar  # build jar file (build/libs/java-api-wrapper-1.x.x.jar)
    $ gradle test # run tests

You don't have to use gradle - the repo also contains a `pom.xml` file which
can be used to build and test the project with [Apache Maven][] (`mvn install`).

Jar files are available in the Github [download section][downloads] and on
sonatype.org / maven ([snapshots][], [releases][], [maven-central][]).

## Examples

The wrapper ships with a few examples in `src/examples/java`:

  * [CreateWrapper][] creates a wrapper and obtains an access token using
  login / password.
  * [GetResource][] performs a GET request for a resource and prints the
  JSON result.
  * [PutResource][] performs a PUT request to update a resource and prints the
  JSON result
  * [UploadFile][] uploads a file to SoundCloud.

You can use gradle tasks to compile and run these examples with one command.
If you don't want to use gradle there is also a precompiled jar with all
dependencies available ([jar-all][]).

First create a wrapper and remember to substitute all credentials with real ones
([register an app][register-app] if you need client_id/secret):

    # with gradle
    $ gradle createWrapper -Pclient_id=my_client_id \
        -Pclient_secret=mys3cr3t \
        -Plogin=api-testing \
        -Ppassword=testing

    # with plain java
    $ java -classpath java-api-wrapper-1.x.y-all.jar \
        com.soundcloud.api.examples.CreateWrapper \
        my_client_id mys3cr3t api-testing testing

Output:

    got token from server: Token{
      access='0000000KNYbSTHKNZC2tq7Epkgxvgmhu',
      refresh='0000000jd4YCL0vCuKf6UtPsS6Ahd0wc', scope='null',
      expires=Mon May 02 17:35:15 CEST 2011}

    wrapper serialised to wrapper.ser

With the wrapper and all tokens serialised to `wrapper.ser` you can run the
other examples.

GET a resource:

    $ gradle getResource -Presource=/me
    (java -classpath java-api-wrapper-1.x.y-all.jar \
        com.soundcloud.api.examples.GetResource /me)

Output:

    GET /me
    {
        "username": "testing",
        "city": "Berlin"
    ...

PUT a resource:

    $ gradle putResource -Presource=/me -Pcontent='{ "user": { "city": "Testor" } }'

Output:

    PUT /me
    {
        "username": "testing",
        "city": "Testor"
    ...

Upload a file:

    $ gradle uploadFile \
            -Pfile=src/test/resources/com/soundcloud/api/hello.aiff
      (java -classpath java-api-wrapper-1.x.y-all.jar \
        com.soundcloud.api.examples.UploadFile ...)

Output:

    Uploading src/test/resources/com/soundcloud/api/hello.aiff
    .............................................
    201 Created https://api.sandbox-soundcloud.com/tracks/2100052
    {
        "artwork_url": null,
        ...

You can add the debug flag (`-d`) to gradle to get some extra HTTP logging:

    $ gradle getResource -Presource=/me -d

    [DEBUG] DefaultClientConnection - Sending request: GET /me HTTP/1.1
    [DEBUG] headers - >> GET /me HTTP/1.1
    [DEBUG] headers - >> Authorization: OAuth 0000000ni3Br147FO7Cj5XotAy
    ...

Note that while the example code uses standard Java serialization to persist
state across calls you should probably use a different mechanism in your app.

## Note on Patches/Pull Requests

  * Fork the project.
  * Make your feature addition or bug fix.
  * Add tests for it.
  * Commit, do not mess with buildfile, version, or history.
  * Send a pull request. Bonus points for topic branches.

If you want to work on the code in an IDE instead of a text editor you can
easily create project files with gradle:

    $ gradle idea     # Intellij IDEA
    $ gradle eclipse  # Eclipse

## Credits / License

The API is based on [urbanstew][]'s [soundcloudapi-java][] project.

Includes portions of code (c) 2010 Xtreme Labs and Pivotal Labs and (c) 2009 urbanSTEW.

See LICENSE for details.

[gradle]: http://www.gradle.org/
[urbanstew]: http://urbanstew.org/
[Apache HttpClient]: http://hc.apache.org/httpcomponents-client-ga/
[HttpMime]: http://hc.apache.org/httpcomponents-client-ga/httpmime
[json-java]: http://json.org/java/
[javadoc]: http://soundcloud.github.com/java-api-wrapper/javadoc/1.0.1/com/soundcloud/api/package-summary.html
[soundcloudapi-java]: http://code.google.com/p/soundcloudapi-java/
[soundcloudapi-java-annouce]: http://blog.soundcloud.com/2010/01/08/java-wrapper/
[CreateWrapper]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/CreateWrapper.java
[GetResource]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/GetResource.java
[PutResource]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/PutResource.java
[UploadFile]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/UploadFile.java
[SoundCloud Android]: https://market.android.com/details?id=com.soundcloud.android
[register-app]: http://soundcloud.com/you/apps/new
[Apache Maven]: http://maven.apache.org/
[jar-all]: https://github.com/downloads/soundcloud/java-api-wrapper/java-api-wrapper-1.0.1-all.jar
[downloads]: https://github.com/soundcloud/java-api-wrapper/archives/master
[snapshots]: https://oss.sonatype.org/content/repositories/snapshots/com/soundcloud/java-api-wrapper/
[releases]: https://oss.sonatype.org/content/repositories/releases/com/soundcloud/java-api-wrapper/
[maven-central]: http://repo1.maven.org/maven2/com/soundcloud/java-api-wrapper/
