FROM gradle:8.3.0-jdk17 AS build

WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .

RUN ./gradlew dependencies --no-daemon

COPY . .

RUN ./gradlew bootJar --no-daemon

FROM amazoncorretto:17-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8070

# 예시
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=90.0", "-jar", "app.jar"]
