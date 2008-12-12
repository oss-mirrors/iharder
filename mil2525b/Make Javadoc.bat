rmdir /s /q api
mkdir api
cd src
javadoc -d ../api -windowtitle "Mil2525b Imagery" mil2525b

cd ..
c:\windows\explorer.exe api\index.html