@echo off
echo ========================================
echo LANCEMENT SANS MAVEN - DIRECT JAVA
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Java utilise:
java -version

echo.
echo Pour l'instant, Maven doit etre installe.
echo Telecharge Maven depuis: https://maven.apache.org/download.cgi
echo.
echo Instructions:
echo 1. Telecharge "Binary zip archive"
echo 2. Extraire dans C:\Program Files\apache-maven
echo 3. Ajouter C:\Program Files\apache-maven\bin au PATH Windows
echo.
echo Une fois Maven installe, utilise run.bat
echo.
pause
