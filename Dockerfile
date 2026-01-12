# ============================================
# Stage 1: Maven 构建阶段
# ============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建项目（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -B && \
    mv target/*.jar app.jar


# ============================================
# Stage 2: 运行阶段
# ============================================
FROM eclipse-temurin:21-jre-alpine

# 设置维护者信息
LABEL maintainer="zhibin.jiang"
LABEL app.name="pregnancy-meal-assistant"
LABEL app.version="1.0.0"

# 设置工作目录
WORKDIR /app

# 创建非 root 用户（最小权限运行）
RUN addgroup -S app && adduser -S -G app app

# 设置时区为亚洲/上海（并保留 CA 证书，避免 HTTPS 调用失败）
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata ca-certificates wget && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo "${TZ}" > /etc/timezone && \
    update-ca-certificates && \
    apk del tzdata

# 创建日志目录
RUN mkdir -p /app/logs && \
    chown -R app:app /app && \
    chmod 755 /app/logs

# 从构建阶段复制 jar 包
COPY --from=builder --chown=app:app /build/app.jar /app/app.jar

# 暴露应用端口
EXPOSE 8080

# JVM 参数配置
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=75.0 \
    -XX:MetaspaceSize=256m \
    -XX:MaxMetaspaceSize=512m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Shanghai"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/api/v3/api-docs || exit 1

# 使用非 root 用户运行
USER app

# 启动应用
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]