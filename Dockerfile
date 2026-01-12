# 使用 Eclipse Temurin JRE 21 (轻量级 Alpine 版本)
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
RUN apk add --no-cache tzdata ca-certificates && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo "${TZ}" > /etc/timezone && \
    update-ca-certificates && \
    apk del tzdata

# 创建日志目录
RUN mkdir -p /app/logs && \
    chown -R app:app /app && \
    chmod 755 /app/logs

# 复制 jar 包到容器
COPY --chown=app:app target/*.jar /app/app.jar

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