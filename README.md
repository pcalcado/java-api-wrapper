# java-api-wrapper

OAuth2 compatible SoundCloud API wrapper

## Build + Test

    # OSX
    $ brew install gradle
    $ gradle jar      # build jar file
    $ gradle test     # run tests

## Generate project files for various IDEs

If you want to work on the code in an IDE instead of a text editor you can
easily create project files with gradle:

    $ gradle idea     # Intellij IDEA
    $ gradle eclipse  # Eclipse

However, never commit any project files to the repo.

## Examples

The wrapper ships with two example classes in `src/examples/java` -
[CreateWrapper.java][] creates a wrapper an obtains an access token using
login/password. The wrapper is then serialised to disk and used by subsequent calls of
[GetResource.java][]. You can use two gradle tasks to run these programs:

    $ gradle createWrapper -Pclient_id=my_client_id \
        -Pclient_secret=mys3cr3t \
        -Plogin=api-testing \
        -Ppassword=testing \
        -Penv=live  # or sandbox

    $ gradle getResource -Presource=/me

## Note on Patches/Pull Requests

  * Fork the project.
  * Make your feature addition or bug fix.
  * Add tests for it.
  * Commit, do not mess with buildfile, version, or history.
  * Send a pull request. Bonus points for topic branches.

## Credits / License

Includes portions of code (c) 2010 Xtreme Labs and Pivotal Labs and (c) 2009 urbanSTEW
See LICENSE for details.

[CreateWrapper.java]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/CreateWrapper.java
[GetResource.java]: https://github.com/soundcloud/java-api-wrapper/blob/master/src/examples/java/com/soundcloud/api/examples/GetResource.java
