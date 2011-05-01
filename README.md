# java-api-wrapper

OAuth2 compatible SoundCloud API wrapper

## Build + Test

The java-api-wrapper used the build system [gradle][]:

    $ brew install gradle (OSX+homebrew)
    $ git clone git://github.com/soundcloud/java-api-wrapper.git
    $ cd java-api-wrapper
    $ gradle jar      # build jar file
    $ gradle test     # run tests

You don't have to use gradle - the wrapper comes with a `pom.xml` file which
can be used to build and test the project (`mvn install`).

## Generate project files for various IDEs

If you want to work on the code in an IDE instead of a text editor you can
easily create project files with gradle:

    $ gradle idea     # Intellij IDEA
    $ gradle eclipse  # Eclipse

Please refrain from committing any IDE configuration files to the repo, as
these can easily be regenerated.

## Examples

The wrapper ships with some examples in `src/examples/java`:

  * [CreateWrapper][] creates a wrapper and obtains an access token using
login / password combination. The wrapper is then serialised to disk and can be used by the
other examples.
  * [GetResource][] performs a GET request for a resource and prints the
  JSON result.
  * [UploadFile][] uploads a file to SoundCloud.

You can use gradle tasks to compile and run these examples with one command:

    $ gradle createWrapper -Pclient_id=my_client_id \
        -Pclient_secret=mys3cr3t \
        -Plogin=api-testing \
        -Ppassword=testing \
        -Penv=live  # or sandbox

    $ gradle getResource -Presource=/me
    $ gradle uploadFile  -Pfile=src/test/resources/com/soundcloud/api/hello.aiff

## Note on Patches/Pull Requests

  * Fork the project.
  * Make your feature addition or bug fix.
  * Add tests for it.
  * Commit, do not mess with buildfile, version, or history.
  * Send a pull request. Bonus points for topic branches.

## Credits / License

Includes portions of code (c) 2010 Xtreme Labs and Pivotal Labs and (c) 2009 urbanSTEW.

See LICENSE for details.

[gradle]: http://www.gradle.org/
[CreateWrapper]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/CreateWrapper.java
[GetResource]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/GetResource.java
[UploadFile]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/UploadFile.java
