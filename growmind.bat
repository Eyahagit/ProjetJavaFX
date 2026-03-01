@echo off
echo ========================================
echo GROWMIND - LANCEMENT CORRIGE
echo ========================================

REM Aller dans le dossier du projet (dossier du .bat)
cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Verification Java:
java -version

echo.
echo IMPORTANT: On utilise UNIQUEMENT les JAR JavaFX "-win.jar".

echo.
echo Etape 1: Build + dependencies...
mvn -q clean package
if %ERRORLEVEL% NEQ 0 (
  echo.
  echo ECHEC BUILD. Copie-colle ce qui est affiche au-dessus.
  pause
  exit /b 1
)

echo.
mvn -q dependency:copy-dependencies -DoutputDirectory=target\lib
if %ERRORLEVEL% NEQ 0 (
  echo.
  echo ECHEC COPY-DEPENDENCIES. Copie-colle ce qui est affiche au-dessus.
  pause
  exit /b 1
)

echo.
echo Etape 2: Nettoyage des anciens JAR JavaFX (stubs) ...
del /q target\lib\javafx-base-21.jar 2>nul
del /q target\lib\javafx-graphics-21.jar 2>nul
del /q target\lib\javafx-controls-21.jar 2>nul
del /q target\lib\javafx-fxml-21.jar 2>nul

echo.
echo Contenu de target\lib :
dir target\lib

echo.
echo Lancement JavaFX (module-path) ...
java --module-path "target\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes" com.santebienetre.App

echo.
echo Exit code: %ERRORLEVEL%
if %ERRORLEVEL% NEQ 0 (
  echo.
  echo ECHEC. Merci de copier-coller TOUT ce qui est affiche dans cette fenetre.
)

echo.
echo ========================================
echo GrowMind - Design moderne pret!
echo ========================================
pause
