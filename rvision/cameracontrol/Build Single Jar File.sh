#!/bin/sh

rm -rf build
mkdir build

# Extract RXTXcomm.jar to build dir and copy native libs
cd build
jar xf ../lib/RXTXcomm.jar
cp ../lib/librxtx* .
cp ../lib/rxtxSerial.dll .
cd ..

# Compile main code into build/classes
cd src
javac -source 1.5 -target 1.5 -d ../build  -classpath ../build rvision/*.java
cd ..

# Copy extra files
cd src
cp rvision/*.properties ../build/rvision
cd ..

# Make jar file of main code
echo Main-Class: rvision.CLI> manifest.txt
cd build
jar cmf ../manifest.txt ../rvision.jar *
cd ..
rm manifest.txt
