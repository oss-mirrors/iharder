#!/bin/sh

rm -rf build
mkdir build


# Compile main code into build/classes
cd src
javac -source 1.5 -target 1.5 -d ../build  -classpath ../build net/iharder/gauge/*.java
cd ..

# Copy images
cd src
cp net/iharder/gauge/*.png ../build/net/iharder/gauge
cp net/iharder/gauge/*.gif ../build/net/iharder/gauge
cp net/iharder/gauge/*.jpg ../build/net/iharder/gauge
cd ..

# Make jar file of main code
echo Main-Class: net.iharder.gauge.Example> manifest.txt
cd build
jar cmf ../manifest.txt ../Gauges.jar *
cd ..
rm manifest.txt
