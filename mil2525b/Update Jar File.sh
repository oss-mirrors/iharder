rm -rf build
mkdir build
mkdir build/mil2525b
cp src/mil2525b/CoTtypes.xml build/mil2525b
cp src/mil2525b/index.php build/mil2525b

cd src
javac -source 1.5 -target 1.5 -d ../build  -classpath ../build mil2525b/*.java
cd ..

echo Main-Class: mil2525b.HttpServerGui> manifest.txt
cp mil2525b-graphics-only.jar mil2525b.jar
cd build
jar umf ../manifest.txt ../mil2525b.jar mil2525b
cd ..
rm manifest.txt
