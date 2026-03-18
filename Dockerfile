# 1. build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# gradle wrapper & 설정만 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 실행권한
RUN chmod +x gradlew

# 의존성 캐시 (빌드 ❌)
RUN ./gradlew --no-daemon dependencies

# 실제 소스 복사
COPY src src

# 빌드
RUN ./gradlew --no-daemon clean bootJar -x test

# 2. run stage
FROM eclipse-temurin:21-jre AS runner
WORKDIR /app

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]