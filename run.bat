@echo off
echo ========================================
echo LANCEMENT APPLICATION GROWMIND
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Verification de l'environnement:
echo Java: 
java -version
echo.
echo Maven:
mvn -version
echo.

echo Nettoyage et compilation...
mvn clean compile
echo.

echo Lancement de l'application...
mvn exec:java -Dexec.mainClass="com.santebienetre.App" -Dexec.args="--module-path=%USERPROFILE%\.m2\repository\org\openjfx" -q

echo.
echo ========================================
echo Si l'application ne s'est pas lancee, essayez:
echo 1. mvn clean package
echo 2. java -cp target/classes;target/lib/* com.santebienetre.App
echo ========================================
pause
