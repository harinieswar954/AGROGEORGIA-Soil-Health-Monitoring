@echo off
if not exist out mkdir out
echo Compiling...
javac -encoding UTF-8 -cp "lib\*;src" -d out src\com\agrogeorgia\database\DatabaseConnection.java src\com\agrogeorgia\model\SoilReading.java src\com\agrogeorgia\ai\CropPrediction.java src\com\agrogeorgia\server\WebServer.java src\com\agrogeorgia\Main.java
if errorlevel 1 (echo Compilation failed. Check lib JAR files. & pause & exit /b 1)
echo Starting...
java -cp "out;lib\*" com.agrogeorgia.Main
pause
