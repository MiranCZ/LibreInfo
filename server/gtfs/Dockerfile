FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,id=gradle,target=/root/.gradle ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
