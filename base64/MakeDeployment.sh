#!/bin/sh

rm -rf deploy
mkdir deploy
./MakeJavadoc.sh
cp -R api deploy/api
cp src/Base64.java deploy
cd deploy
zip ../Base64-vX.X.X.zip Base64.java api
cd ..
rm -rf deploy
