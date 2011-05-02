# java-api-wrapper

OAuth2 compatible SoundCloud API wrapper ([javadoc][]).

## Build + Test

The java-api-wrapper uses the build system [gradle][]:

    $ brew install gradle (OSX+homebrew, check website for other OS)
    $ git clone git://github.com/soundcloud/java-api-wrapper.git
    $ cd java-api-wrapper
    $ gradle jar      # build jar file (build/libs/java-api-wrapper-1.x.x.jar)
    $ gradle test     # run tests

You don't have to use gradle - the repo contains a `pom.xml` file which
can be used to build and test the project with Maven (`mvn install`).

## Examples

The wrapper ships with some examples in `src/examples/java`:

  * [CreateWrapper][] creates a wrapper and obtains an access token using
login / password combination. The wrapper is then serialised to disk and can be used by the
other examples, so you have to run this one first.
  * [GetResource][] performs a GET request for a resource and prints the
  JSON result.
  * [UploadFile][] uploads a file to SoundCloud.

You can use gradle tasks to compile and run these examples with one command.
First create a wrapper, remember to replace all credentials with real ones.

    $ gradle createWrapper -Pclient_id=my_client_id \
        -Pclient_secret=mys3cr3t \
        -Plogin=api-testing \
        -Ppassword=testing \
        -Penv=live  # or sandbox

    got token from server: Token{access='0000000KNYbSTHKNZC2tq7Epkgxvgmhu', refresh='0000000jd4YCL0vCuKf6UtPsS6Ahd0wc', scope='null', expires=Mon May
    02 17:35:15 CEST 2011}
    wrapper serialised to wrapper.ser

With the wrapper and all tokens serialised to `wrapper.ser` you can run the
other examples:

    $ gradle getResource -Presource=/me
    GET /me
    {
        "avatar_url": "http://i1.sndcdn.com/avatars-000000599474-nv71y5-large.jpg?af2741b",
        "city": "Berlin"
    ...

    $ gradle uploadFile  -Pfile=src/test/resources/com/soundcloud/api/hello.aiff
    Uploading src/test/resources/com/soundcloud/api/hello.aiff
    .............................................
    201 Created https://api.sandbox-soundcloud.com/tracks/2100052
    {
        "artwork_url": null,
        ...

You can add the debug flag (`-d`) to gradle to enable extra HTTP logging:

    $ gradle getResource -Presource=/me -d

    2011/05/02 02:03:44:263 CEST [DEBUG] DefaultClientConnection - Sending request: GET /me HTTP/1.1
    2011/05/02 02:03:44:265 CEST [DEBUG] headers - >> GET /me HTTP/1.1
    2011/05/02 02:03:44:265 CEST [DEBUG] headers - >> Authorization: OAuth 0000000ni3Br147FO7Cj5Xotqg5hAyxx
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
[javadoc]: http://soundcloud.github.com/java-api-wrapper/javadoc/package-summary.html
[soundcloudapi-java]: http://code.google.com/p/soundcloudapi-java/
[CreateWrapper]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/CreateWrapper.java
[GetResource]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/GetResource.java
[UploadFile]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/UploadFile.java
