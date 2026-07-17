# ================================================
# 多阶段构建：构建阶段（使用Maven基础镜像）
# 基础镜像：Maven 3.9.6 + Eclipse Temurin JDK21
# ================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
# 设置容器内工作目录
WORKDIR /app

# ------------------------------------------------
# 复制宿主机构建上下文全部文件到容器工作目录
# ------------------------------------------------
COPY . .

# ================================================
# Maven构建命令（含优化参数）
# 主要参数说明：
#   -DskipTests=true              : 跳过单元测试
#   -Dmaven.test.skip=true        : 跳过测试编译阶段
#   -Dmaven.compile.fork=true     : 启用并行编译
#   -Dmaven.compiler.source/target: 指定Java版本
#   -Dmaven.compiler.force...     : 强制使用javac
#   -annotationProcessorPaths     : 配置Lombok注解处理器
# ================================================
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

# ================================================
# 多阶段构建：运行阶段（使用轻量级JRE镜像）
# 基础镜像：Eclipse Temurin JRE21 + Ubuntu Jammy
# ================================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# ------------------------------------------------
# 从构建阶段复制生成的JAR文件
# ARG参数说明：
#   JAR_FILE : 匹配target目录下的所有JAR文件
# ------------------------------------------------
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# ------------------------------------------------
# 时区配置（设置为亚洲/上海时区）
# 通过符号链接和配置文件修改容器时区
# ------------------------------------------------
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# ================================================
# JVM参数配置（内存管理与GC优化）
# 关键参数说明：
#   -Xms/-Xmx          : 堆内存初始/最大值
#   -XX:MetaspaceSize  : 元空间初始大小
#   -UseG1GC           : 启用G1垃圾回收器
#   -MaxGCPauseMillis  : 目标最大GC停顿时间
#   -HeapDumpOn...     : OOM时生成堆转储
#   -UseContainerSupport : 容器内存感知
#   -MaxRAMPercentage  : 最大内存占用比例
# ================================================
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

# ------------------------------------------------
# 声明容器暴露端口（HTTP服务默认端口）
# ------------------------------------------------
EXPOSE 8080

# ================================================
# 容器启动入口命令
# 使用sh -c执行命令以支持环境变量扩展
# 最终启动命令：java [JVM参数] -jar app.jar
# ================================================
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]