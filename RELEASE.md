#How to release a new version of the wrapper

Bump version constants in

 * build.gradle
 * src/main/java/com/soundcloud/api/package-info.java
 * src/main/java/com/soundcloud/api/CloudAPI.java

Regenerate + publish javadoc:

    $ ./update_javadoc.sh

Regenerate pom.xml

    $ gradle writePom

Upload to oss sonatype (snapshot)

    $ gradle uploadArchives

Releasing to oss sonatype (staging)

    $ mvn -DdryRun=true -Dresume=false -DskipTests release:prepare
    $ mvn release:perform -Darguments="-Dgpg.keyname=jan@soundcloud.com -Dgpg.passphrase="
