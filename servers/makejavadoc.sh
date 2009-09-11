#!/bin/sh
rm -rf api
mkdir api
javadoc -d api -author -doctitle "Simple Servers from iHarder.net" -windowtitle "Simple Servers from iHarder.net" -keywords    src/*Server.java
open api/index.html
