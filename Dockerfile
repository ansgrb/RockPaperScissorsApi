# Build stage
FROM gradle:8.5-jdk17 AS build

RUN apt-get update && apt-get install -y ca-certificates && apt-get clean

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN ./gradlew buildFatJar --no-daemon

# Run stage
FROM openjdk:17-jdk-slim
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/ktor-app.jar
ENTRYPOINT ["java","-jar","/app/ktor-app.jar"]