@echo off
echo ========================================
echo TEST MAVEN MANUEL
echo ========================================

echo Test avec ton chemin Maven:
set MAVEN_HOME=C:\Users\iyed\Downloads\apache-maven-3.9.12-bin\apache-maven-3.9.12
set PATH=%MAVEN_HOME%\bin;%PATH%

echo Test Maven:
mvn -version

echo.
echo Si Maven fonctionne, on lance l'application:
pause
mvn javafx:run
