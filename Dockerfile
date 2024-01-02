FROM gradle:8.4.0-jdk20

WORKDIR /app

COPY ./ .

CMD ./gradlew build && ./gradlew installDist && ./build/install/app/bin/app
