# 构建阶段
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 首先只复制pom文件，利用Maven的依赖缓存
COPY pom.xml .
# 下载依赖到本地缓存，使用并行下载
RUN mvn dependency:go-offline -B -Dmaven.repo.local=/root/.m2/repository

# 复制源代码
COPY src ./src
# 构建应用，使用并行构建和跳过测试，优化构建参数
RUN mvn clean package -DskipTests -T 1C \
    -Dmaven.test.skip=true \
    -Dmaven.compile.fork=true \
    -Dmaven.javadoc.skip=true

# 运行阶段
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 设置构建参数
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置JVM参数
ENV JAVA_OPTS="-Xms2g -Xmx4g \
    -XX:MetaspaceSize=256m \
    -XX:MaxMetaspaceSize=512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:ParallelGCThreads=4 \
    -XX:ConcGCThreads=2 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/dump \
    -Xlog:gc*:file=/app/gc.log \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0"

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 