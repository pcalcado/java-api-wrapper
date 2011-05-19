# java-api-wrapper

OAuth2 SoundCloud API wrapper written in Java ([javadoc][]), extracted from the
[SoundCloud Android][] codebase.

It's lightweight and requires a minimum of external dependencies
- should be easily embeddable in both desktop and mobile applications.

## Basic usage

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

## Examples

The wrapper ships with some examples in `src/examples/java`:

  * [CreateWrapper][] creates a wrapper and obtains an access token using
  login / password.
  * [GetResource][] performs a GET request for a resource and prints the
  JSON result.
  * [UploadFile][] uploads a file to SoundCloud.

You can use gradle tasks to compile and run these examples with one command.

First create a wrapper, remember to substitute all credentials with real ones
([register an app][register-app] if you need client_id/secret):

    $ gradle createWrapper -Pclient_id=my_client_id \
        -Pclient_secret=mys3cr3t \
        -Plogin=api-testing \
        -Ppassword=testing


    got token from server: Token{
      access='0000000KNYbSTHKNZC2tq7Epkgxvgmhu',
      refresh='0000000jd4YCL0vCuKf6UtPsS6Ahd0wc', scope='null',
      expires=Mon May 02 17:35:15 CEST 2011}

    wrapper serialised to wrapper.ser

With the wrapper and all tokens serialised to `wrapper.ser` you can run the
other examples.

GET a resource:

    $ gradle getResource -Presource=/me
    GET /me
    {
        "username": "testing",
        "city": "Berlin"
    ...

Upload a file:

    $ gradle uploadFile \
            -Pfile=src/test/resources/com/soundcloud/api/hello.aiff
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

Please refrain from committing any IDE configuration files to the repo, as
these can easily be regenerated.

## Credits / License

The API is based on [urbanstew][]'s [soundcloudapi-java][] project.

Includes portions of code (c) 2010 Xtreme Labs and Pivotal Labs and (c) 2009 urbanSTEW.

See LICENSE for details.

[gradle]: http://www.gradle.org/
[urbanstew]: http://urbanstew.org/
[Apache HttpClient]: http://hc.apache.org/httpcomponents-client-ga/
[HttpMime]: http://hc.apache.org/httpcomponents-client-ga/httpmime
[json-java]: http://json.org/java/
[javadoc]: http://soundcloud.github.com/java-api-wrapper/javadoc/1.0.0/com/soundcloud/api/package-summary.html
[soundcloudapi-java]: http://code.google.com/p/soundcloudapi-java/
[soundcloudapi-java-annouce]: http://blog.soundcloud.com/2010/01/08/java-wrapper/
[CreateWrapper]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/CreateWrapper.java
[GetResource]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/GetResource.java
[UploadFile]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/UploadFile.java
[SoundCloud Android]: https://market.android.com/details?id=com.soundcloud.android
[register-app]: http://soundcloud.com/you/apps/new
[Apache Maven]: http://maven.apache.org/
