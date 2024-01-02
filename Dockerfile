FROM gradle:8.4.0-jdk20

WORKDIR /app

COPY ./ .

RUN chmod +x gradlew

CMD ./gradlew build && ./gradlew installDist && ./build/install/app/bin/app
