@echo off
echo ========================================
echo LANCEMENT AVEC DEBUG VISUEL
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Test 1: Lancement avec options visuelles forcees...
java -Djava.awt.headless=false -cp "target\classes;target\lib\*" com.santebienetre.App

echo.
echo Test 2: Lancement avec options JavaFX...
java --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports javafx.controls/com.sun.javafx.controls=ALL-UNNAMED -Djava.awt.headless=false -cp "target\classes;target\lib\*" com.santebienetre.App

echo.
echo Test 3: Verification de l'affichage JavaFX...
echo Si rien ne s'affiche, le probleme peut etre:
echo 1. Drivers graphiques
echo 2. Configuration JavaFX
echo 3. Probleme avec Windows 11
echo.
echo Essaie de redemarrer ton ordinateur et relance
echo ========================================
pause
