# 构建阶段
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 复制所有文件
COPY . .

# 构建应用，跳过测试并优化构建参数
RUN mvn clean package \
    -DskipTests=true \
    -Dmaven.test.skip=true \
    -Dmaven.javadoc.skip=true \
    -Dmaven.compile.fork=true \
    -Dmaven.compiler.source=21 \
    -Dmaven.compiler.target=21 \
    -Dmaven.compiler.forceJavacCompilerUse=true \
    -Dmaven.compiler.showWarnings=true \
    -Dmaven.compiler.showDeprecation=true \
    -Dmaven.compiler.annotationProcessorPaths=lombok:org.projectlombok:lombok:1.18.30

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

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 