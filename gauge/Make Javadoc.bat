rmdir /s /q api
mkdir api
cd src
javadoc -d ../api -windowtitle "Gauges"  net.iharder.gauge

cd ..
c:\windows\explorer.exe api\index.html