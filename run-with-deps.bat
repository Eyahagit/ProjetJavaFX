@echo off
echo ========================================
echo LANCEMENT AVEC DEPENDANCES COPIEES
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Creation du dossier lib...
if not exist "target\lib" mkdir "target\lib"

echo Copie des dependances JavaFX...
copy "C:\Users\iyed\.m2\repository\org\openjfx\javafx-controls\21\javafx-controls-21.jar" "target\lib\" >nul 2>&1
copy "C:\Users\iyed\.m2\repository\org\openjfx\javafx-fxml\21\javafx-fxml-21.jar" "target\lib\" >nul 2>&1
copy "C:\Users\iyed\.m2\repository\org\openjfx\javafx-base\21\javafx-base-21.jar" "target\lib\" >nul 2>&1
copy "C:\Users\iyed\.m2\repository\org\openjfx\javafx-graphics\21\javafx-graphics-21.jar" "target\lib\" >nul 2>&1

echo Verification des fichiers copiees:
dir target\lib

echo.
echo Lancement de l'application avec le design GrowMind!
echo.

java -cp "target\classes;target\lib\*" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base com.santebienetre.App

echo.
echo ========================================
echo Si erreur: Verifie que les JAR JavaFX sont dans target\lib
echo ========================================
pause
