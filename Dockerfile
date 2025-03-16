# Java 앱을 위한 Dockerfile - Gradle 프로젝트
FROM gradle:8.6-jdk17 AS build

WORKDIR /app

# Gradle 빌드 캐시 최적화를 위한 파일 복사
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle ./gradle

# 의존성만 먼저 다운로드 (캐싱 최적화)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN gradle build --no-daemon -x test

# 실행 환경 설정
FROM openjdk:17-slim

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 컨테이너 실행 시 실행할 명령어
CMD ["java", "-jar", "app.jar"]