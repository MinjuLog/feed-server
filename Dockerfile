# 1) Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew

# 의존성/gradle 캐시 재사용
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test

# 2) Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]