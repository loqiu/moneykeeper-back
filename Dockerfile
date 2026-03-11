FROM maven:3.9-eclipse-temurin-21 AS build
LABEL authors="Rochelle"

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV TZ=Europe/London
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]