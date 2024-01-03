FROM gradle:8.4.0-jdk20

WORKDIR /app

RUN apt-get update && apt-get install -yq make unzip

COPY /app/config config
COPY /app/gradle gradle
COPY /app/build.gradle .
COPY /app/settings.gradle .
COPY /app/gradlew .

RUN ./gradlew --no-daemon dependencies

COPY /app/src src

RUN ./gradlew --no-daemon build

CMD java -jar build/libs/app-0.0.1-SNAPSHOT.jar
