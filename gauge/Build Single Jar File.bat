@echo off

REM rmdir is part of cygwin - not sure how to do it in DOS
rmdir /s /q build
mkdir build


REM Compile main code into build/classes
cd src
javac -source 1.5 -target 1.5 -d ../build  -classpath ../build net/iharder/gauge/*.java
cd ..

REM Copy images
cd src
COPY net\iharder\gauge\*.png ..\build\net\iharder\gauge
COPY net\iharder\gauge\*.gif ..\build\net\iharder\gauge
COPY net\iharder\gauge\*.jpg ..\build\net\iharder\gauge
cd ..

REM Make jar file of main code
echo Main-Class: net.iharder.gauge.Example> manifest.txt
cd build
jar cmf ../manifest.txt ../Gauges.jar *
cd ..
rm manifest.txt


pause