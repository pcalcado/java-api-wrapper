#How to release a new version of the wrapper

Bump version constants in

 * build.gradle
 * src/main/java/com/soundcloud/api/package-info.java
 * src/main/java/com/soundcloud/api/CloudAPI.java

Regenerate + publish javadoc:

    $ ./update_javadoc.sh

Regenerate pom.xml

    $ gradle writePom

Upload to to oss sonatype

    $ gradle uploadArchives

Signing (does not work yet)

    $ mvn gpg:sign-and-deploy-file -DpomFile=build/poms/pom-default.xml \
      -Dfile=build/libs/java-api-wrapper-1.0.0.jar \
      -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
      -DrepositoryId=sonatype-nexus-staging \
      -Dgpg.keyname=jan@soundcloud.com
