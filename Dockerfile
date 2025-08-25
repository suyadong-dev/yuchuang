# 使用官方 JDK 作为基础镜像
# FROM eclipse-temurin:21-jdk
FROM openjdk:21-jdk

# 设置容器时区
ENV TZ=Asia/Shanghai

# 在容器里建个目录放应用
WORKDIR /app

# 把 jar 包和配置文件复制进去
COPY app.jar app.jar
COPY application-prod.yml application-prod.yml

# 运行 jar 时，指定 Spring 环境 = prod
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]