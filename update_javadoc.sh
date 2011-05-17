#!/bin/sh -x

## updates the javadocs hosted at
## http://soundcloud.github.com/java-api-wrapper/javadoc/com/soundcloud/api/package-summary.html
## by regenerating them via gradle and pushing them back to gh-pages

DOCS=docs

set -e

trap "rm -rf $PWD/$DOCS" EXIT

rm -rf $DOCS
git clone . $DOCS -b gh-pages
gradle javadoc
rsync -f 'exclude .git' -r --delete build/docs/ $DOCS
cd $DOCS
git commit -m 'javadoc update' -a --allow-empty
git add .
git commit --amend -a -m 'javadoc update' --allow-empty
git push origin gh-pages
