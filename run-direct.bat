@echo off
echo ========================================
echo LANCEMENT DIRECT SANS MAVEN
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Verification Java:
java -version

echo.
echo Compilation des classes Java...
if not exist "target\classes" mkdir "target\classes"

echo Compilation en cours...
javac -d "target\classes" -cp "lib\*" src\main\java\com\santebienetre\*.java src\main\java\com\santebienetre\controller\*.java src\main\java\com\santebienetre\dao\*.java src\main\java\com\santebienetre\model\*.java src\main\java\com\santebienetre\service\*.java src\main\java\com\santebienetre\util\*.java

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Echec de la compilation
    pause
    exit /b 1
)

echo.
echo Lancement de l'application...
java -cp "target\classes;lib\*" --add-modules javafx.controls,javafx.fxml com.santebienetre.App

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Echec du lancement
    echo Essayons de telecharger les dependances JavaFX...
    echo.
    echo Solution: Installer Maven et utiliser run.bat
    echo Ou: Configurer manuellement JavaFX
    echo.
)

pause
