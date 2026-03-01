@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin;%PATH%

echo Lancement de l'application GrowMind...
java -Djava.awt.headless=false -cp "target\classes;target\lib\*" com.santebienetre.App

if %ERRORLEVEL% EQU 0 (
    echo Succes! L'application est lancee.
) else (
    echo Erreur detected. Tentative avec options alternatives...
    java -cp "target\classes;target\lib\*" com.santebienetre.App
)
pause
