@echo off
echo ========================================
echo INSERTION DONNEES DEMO - GROWMIND
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Insertion donnees Santé & Bien-être...
java -cp "target\classes;target\lib\*" com.santebienetre.util.DatabaseInit

echo.
echo Insertion donnees Sommeil...
java -cp "target\classes;target\lib\*" com.santebienetre.util.DatabaseInit

echo.
echo Lancement de l'application...
java --module-path "target\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes" com.santebienetre.App

pause
