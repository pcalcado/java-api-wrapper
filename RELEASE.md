#How to release a new version of the wrapper

Requirements:

 * Your gpg key is registered on a public keyserver (see [How To Generate PGP Signatures With Maven][])
 * You have an account on Sonatype OSS (see [Sonatype OSS Maven Repository Usage Guide][])

Bump version constants in

 * build.gradle
 * src/main/java/com/soundcloud/api/package-info.java
 * src/main/java/com/soundcloud/api/CloudAPI.java

Regenerate + publish javadoc:

    $ ./update_javadoc.sh

Regenerate pom.xml

    $ gradle writePom

Releasing to Sonatype OSS (staging)

    (make sure there are no uncommitted changes in the repo)
    $ mvn -Dresume=false release:prepare # tag repo, bump pom.xml
    $ mvn release:perform -Darguments="-Dgpg.keyname=jan@soundcloud.com -Dgpg.passphrase="

This will build and sign all artifcats and upload them to the staging server.
In order to release you need to login to [Sonatype OSS][], from the "Build
Promotion" tab on the left hand site select "Staging Repositories". The release
you just uploaded should show up in the list. Select it and pick "Close". This
will check if the deployment is complete and properly signed, then create a
staging repository which can be used for testing. Once everything works you
select "Release" to actually release it to the [release repo][]. The release
repo is synced with [Maven Central][].

Releasing snapshot versions

This is for releasing developer version of the package and can be done anytime,
just make sure `build.gradle` version contains a `-SNAPSHOT` suffix, then run:

    $ gradle uploadArchive

Snapshots can be found in the [snapshot repo][].

[Sonatype OSS Maven Repository Usage Guide]: https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
[Sonatype OSS]: https://oss.sonatype.org/
[How To Generate PGP Signatures With Maven]: https://docs.sonatype.org/display/Repository/How+To+Generate+PGP+Signatures+With+Maven
[release repo]: https://oss.sonatype.org/content/repositories/releases/com/soundcloud/java-api-wrapper/
[snapshot repo]: https://oss.sonatype.org/content/repositories/snapshots/com/soundcloud/java-api-wrapper/
[Maven Central]: http://repo1.maven.org/maven2/
