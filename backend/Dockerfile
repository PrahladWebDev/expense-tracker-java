# Multi-stage build: stage 1 compiles with Maven + JDK, stage 2 runs on a
# slim JRE-only image so the final container doesn't ship the entire build
# toolchain (smaller image, smaller attack surface).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
