# Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app
# 먼저 라이브러리를 다운받으면, 나중에 소스 코드만 바꿨을 때 라이브러리 다운로드를 다시 안 해도 됨(캐시 가능)
# Docker는 레이어를 쌓아서 이미지를 만듬. 한 레이어가 바뀌면 그 안래 모든 레이어를 바꿔야 하기 때문에, 가장 변경 가능한 코드 부분을 마지막으로 하고 변경을 거의 하지 않는 부분을 먼저 COPY함
COPY build.gradle settings.gradle ./
# Gradle 버전을 사용하게 해주는 파일(캐시 가능)
COPY gradle ./gradle
# build.gradle에 적힌 라이브러리들을 다운로드 || 컨테이너는 일회용이라 데몬이 필요 없음(캐시 가능)
RUN gradle dependencies --no-daemon || true
# 소스 코드 복사
COPY src ./src
# 테스트를 건너뛰며 소스 코드를 컴파일해서 JAR 파일을 만듬
RUN gradle bootJar --no-daemon -x test

# Run stage
# JRE → JDK로 변경 (VisualVM 전체 기능 지원을 위해)
FROM eclipse-temurin:21-jdk

WORKDIR /app
# Build 스테이지에 있는 jar를 해당 컨테이너로 가져옴
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
EXPOSE 9010

ENTRYPOINT ["java", \
  "-Dcom.sun.management.jmxremote", \
  "-Dcom.sun.management.jmxremote.port=9010", \
  "-Dcom.sun.management.jmxremote.rmi.port=9010", \
  "-Dcom.sun.management.jmxremote.local.only=false", \
  "-Dcom.sun.management.jmxremote.authenticate=false", \
  "-Dcom.sun.management.jmxremote.ssl=false", \
  "-Djava.rmi.server.hostname=localhost", \
  "-jar", "app.jar"]
