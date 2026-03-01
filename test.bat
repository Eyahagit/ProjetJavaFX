@echo off
echo ========================================
echo TEST SIMPLE - GARDER FENETRE OUVERTE
echo ========================================

echo Etape 1: Configuration Java...
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Etape 2: Test Java...
java -version > java_version.txt 2>&1
type java_version.txt

echo.
echo Etape 3: Test repertoire...
cd

echo.
echo Etape 4: Test compilation simple...
javac -version > javac_version.txt 2>&1
type javac_version.txt

echo.
echo ========================================
echo FIN DES TESTS - FENETRE RESTE OUVERTE
echo ========================================
echo.
echo Verifie les fichiers java_version.txt et javac_version.txt
echo.
pause
