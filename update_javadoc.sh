#!/bin/sh

## updates the javadocs hosted at
## http://soundcloud.github.com/java-api-wrapper/javadoc/com/soundcloud/api/package-summary.html
## by regenerating them via gradle and pushing them back to gh-pages

DOCS=docs
JAVADOCS=build/javadoc

set -e

trap "rm -rf $PWD/$DOCS" EXIT

rm -rf $DOCS $JAVADOCS
git clone git@github.com:soundcloud/java-api-wrapper.git $DOCS -b gh-pages
gradle doc
VERSION=$(basename $JAVADOCS/*)
rsync -f 'exclude .git' -r --delete $JAVADOCS/$VERSION $DOCS/javadoc
cd $DOCS
git commit -m "javadocs for $VERSION" -a --allow-empty
git add .
git commit --amend -a -m "javadocs for $VERSION" --allow-empty
git diff origin/gh-pages --summary --exit-code || git push origin gh-pages
