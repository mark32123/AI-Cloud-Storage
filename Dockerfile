# 构建阶段
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 首先只复制pom文件，利用Maven的依赖缓存
COPY pom.xml .
# 下载依赖到本地缓存
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src
# 构建应用，使用并行构建和跳过测试
RUN mvn clean package -DskipTests -T 1C

# 运行阶段
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"] 