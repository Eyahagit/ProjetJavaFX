@echo off
echo ========================================
echo LANCEMENT SANS MODULES (CLASSPATH)
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Lancement avec classpath etendu...
java -cp "target\classes;target\lib\*" com.santebienetre.App

echo.
echo Si erreur, essayons avec les JAR Windows specifiques:
java -cp "target\classes;target\lib\javafx-base-21-win.jar;target\lib\javafx-controls-21-win.jar;target\lib\javafx-fxml-21-win.jar;target\lib\javafx-graphics-21-win.jar;target\lib\mysql-connector-j-8.4.0.jar;target\lib\protobuf-java-3.25.1.jar" com.santebienetre.App

echo.
echo ========================================
echo Fin du test
echo ========================================
pause
