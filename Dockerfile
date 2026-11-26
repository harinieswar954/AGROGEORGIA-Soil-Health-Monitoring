
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN mkdir -p out && javac -encoding UTF-8 -cp "lib/*:src" -d out \
    src/com/agrogeorgia/database/DatabaseConnection.java \
    src/com/agrogeorgia/model/SoilReading.java \
    src/com/agrogeorgia/ai/CropPrediction.java \
    src/com/agrogeorgia/server/WebServer.java \
    src/com/agrogeorgia/Main.java

EXPOSE 8080

CMD ["java", "-cp", "out:lib/*", "com.agrogeorgia.Main"]
