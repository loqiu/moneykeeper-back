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

# 设置时区 (非常重要！否则日志时间会差8小时)
ENV TZ=Europe/London
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口 (和你 application.properties 里的 server.port 对应)
EXPOSE 8081

# 启动命令
# 激活 prod 配置文件 (对应 application-prod.properties)
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]


