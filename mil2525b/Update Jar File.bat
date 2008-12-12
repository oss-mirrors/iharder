@echo off

REM rmdir is part of cygwin - not sure how to do it in DOS
rmdir /s /q build
mkdir build

REM Copy xml
mkdir build\mil2525b
copy src\mil2525b\CoTtypes.xml build\mil2525b
copy src\mil2525b\index.php build\mil2525b

REM Compile main code into build/classes
cd src
javac -source 1.5 -target 1.5 -d ../build  -classpath ../build mil2525b/*.java
cd ..

REM Make jar file of main code
echo Main-Class: mil2525b.HttpServerGui> manifest.txt
copy mil2525b-graphics-only.jar mil2525b.jar
cd build
jar umf ../manifest.txt ../mil2525b.jar mil2525b
cd ..
rm manifest.txt


pause