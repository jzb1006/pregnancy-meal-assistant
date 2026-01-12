# 孕妇今天吃啥 - Docker 部署指南

## 📋 前置要求

### 服务器环境要求

- **操作系统**: Linux (推荐 Ubuntu 20.04+ / CentOS 8+)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **内存**: 最低 2GB，推荐 4GB+
- **磁盘**: 最低 10GB 可用空间

### 安装 Docker（如果尚未安装）

**Ubuntu/Debian:**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

**CentOS/RHEL:**
```bash
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo systemctl enable docker
```

---

## 🚀 快速部署

### 方式一：直接使用 Docker 镜像（推荐生产环境）

#### 1. 本地打包并上传 jar 包

```bash
# 在本地开发环境打包
mvn clean package -DskipTests

# 上传 jar 包到服务器
scp target/pregnancy-meal-assistant-1.0.0.jar user@your-server:/app/
```

#### 2. 在服务器上创建目录结构

```bash
mkdir -p /app/pregnancy-meal
cd /app/pregnancy-meal
```

#### 3. 上传必要文件到服务器

```bash
# 上传 Dockerfile
scp Dockerfile user@your-server:/app/pregnancy-meal/

# 上传 .env 文件（需要提前配置；用于 app 容器环境变量）
scp .env user@your-server:/app/pregnancy-meal/

# 或者上传 docker-compose.yml
scp docker-compose.yml user@your-server:/app/pregnancy-meal/
```

#### 4. 配置环境变量

```bash
cat > /app/pregnancy-meal/.env << 'EOF'
# 应用数据库连接（方式一：假设你使用"外部/已存在"的 MySQL）
DB_URL=jdbc:mysql://your-mysql-host:3306/pregnancy_meal?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_USERNAME=app_user
DB_PASSWORD=your_strong_app_password

# 阿里云 AI 配置
ALI_AI_KEY=your_dashscope_api_key

# 微信小程序配置
WX_MINIAPP_APPID=your_wechat_appid
WX_MINIAPP_SECRET=your_wechat_secret

# JWT 配置
JWT_SECRET=your_jwt_secret_please_change
JWT_EXPIRATION=604800000
EOF
```

#### 5. 构建 Docker 镜像

```bash
cd /app/pregnancy-meal
docker build -t pregnancy-app:1.0.0 .
```

#### 6. 运行容器

```bash
# 运行应用容器（假设 MySQL 已经运行）
docker run -d \
  --name pregnancy-app \
  --restart always \
  -p 8080:8080 \
  --env-file .env \
  -v /app/pregnancy-meal/logs:/app/logs \
  pregnancy-app:1.0.0
```

---

### 方式二：使用 Docker Compose（推荐包含数据库）

#### 1. 上传文件到服务器

```bash
# 创建目录
mkdir -p /app/pregnancy-meal
cd /app/pregnancy-meal

# 上传以下文件
# - docker-compose.yml
# - .env
# - target/*.jar (jar 包)
# - target/classes/db/migration (MySQL 初始化脚本；仅在首次初始化数据卷时执行)
```

#### 2. 启动所有服务

```bash
cd /app/pregnancy-meal
docker compose up -d
```

#### 3. 查看服务状态

```bash
# 查看运行状态
docker compose ps

# 查看日志
docker compose logs -f app

# 查看所有服务日志
docker compose logs -f
```

---

## 🔧 常用命令

### 容器管理

```bash
# 查看运行中的容器
docker ps

# 查看容器日志
docker logs -f pregnancy-app

# 进入容器内部
docker exec -it pregnancy-app sh

# 停止容器
docker stop pregnancy-app

# 启动容器
docker start pregnancy-app

# 重启容器
docker restart pregnancy-app

# 删除容器
docker rm pregnancy-app

# 强制删除运行中的容器
docker rm -f pregnancy-app
```

### 镜像管理

```bash
# 查看本地镜像
docker images

# 删除镜像
docker rmi pregnancy-app:1.0.0

# 清理无用镜像
docker image prune -a
```

### Docker Compose 命令

```bash
# 启动服务
docker compose up -d

# 停止服务
docker compose down

# 停止并删除数据卷（⚠️ 会删除数据）
docker compose down -v

# 重启服务
docker compose restart

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f [service_name]

# 重新构建镜像
docker compose build --no-cache

# 更新并重启服务
docker compose up -d --build
```

---

## 📊 健康检查

### 检查应用状态

```bash
# 检查（示例）端点：OpenAPI 文档可用性
curl http://localhost:8080/api/v3/api-docs

# 查看 Swagger 文档
curl http://localhost:8080/api/swagger-ui.html
```

### 检查容器健康状态

```bash
# 查看容器健康状态
docker inspect --format='{{.State.Health.Status}}' pregnancy-app

# 查看详细健康检查日志
docker inspect --format='{{json .State.Health}}' pregnancy-app | jq
```

---

## 🔍 故障排查

### 问题 1: 容器启动失败

```bash
# 查看容器日志
docker logs pregnancy-app

# 查看容器详细信息
docker inspect pregnancy-app
```

### 问题 2: 无法连接数据库

```bash
# 检查 MySQL 容器状态
docker ps | grep mysql

# 测试数据库连接
docker exec -it pregnancy-mysql mysql -u app_user -p
```

### 问题 3: 端口被占用

```bash
# 查看端口占用
sudo netstat -tulpn | grep 8080

# 修改 docker-compose.yml 中的端口映射
ports:
  - "8081:8080"  # 使用 8081 端口
```

### 问题 4: 内存不足

```bash
# 查看容器资源使用情况
docker stats pregnancy-app

# 调整 JVM 内存参数
# 在 .env 或 docker-compose.yml 中修改 JAVA_OPTS
JAVA_OPTS="-XX:MaxRAMPercentage=50.0 ..."  # 降低内存使用
```

---

## 🛡️ 生产环境建议

### 1. 安全加固

- ✅ 修改所有默认密码
- ✅ 使用强密码策略
- ✅ 限制容器资源使用
- ✅ 配置防火墙规则
- ✅ 定期更新镜像

### 2. 资源限制

在 `docker-compose.yml` 中添加资源限制：

```yaml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '1.0'
          memory: 1G
```

### 3. 日志管理

```yaml
services:
  app:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 4. 数据备份

```bash
# 备份 MySQL 数据
docker exec pregnancy-mysql mysqldump -u root -p pregnancy_meal > backup.sql

# 恢复数据
docker exec -i pregnancy-mysql mysql -u root -p pregnancy_meal < backup.sql
```

### 5. 监控告警

建议使用以下工具：
- **Prometheus + Grafana**: 性能监控
- **ELK Stack**: 日志分析
- **Sentry**: 错误追踪

---

## 📝 环境变量说明

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| DB_URL | 应用数据库连接串（方式一 docker run 使用） | - | ❌ |
| DB_USERNAME | 应用数据库用户（方式一 docker run 使用） | - | ❌ |
| DB_PASSWORD | 应用数据库密码（方式一 docker run 使用） | - | ❌ |
| MYSQL_ROOT_PASSWORD | MySQL root 密码 | - | ✅ |
| MYSQL_DATABASE | 数据库名称 | pregnancy_meal | ✅ |
| MYSQL_USER | 应用数据库用户 | app_user | ✅ |
| MYSQL_PASSWORD | 应用数据库密码 | - | ✅ |
| ALI_AI_KEY | 阿里云 DashScope API Key | - | ✅ |
| WX_MINIAPP_APPID | 微信小程序 AppID | - | ✅ |
| WX_MINIAPP_SECRET | 微信小程序 Secret | - | ✅ |
| JWT_SECRET | JWT 签名密钥 | - | ✅ |
| JWT_EXPIRATION | JWT 过期时间(ms) | 604800000 | ❌ |
| JAVA_OPTS | JVM 启动参数 | 默认配置 | ❌ |

---

## 🔄 更新部署

### 更新应用版本

```bash
# 1. 停止并删除旧容器
docker compose down

# 2. 删除旧镜像
docker rmi pregnancy-app:1.0.0

# 3. 上传新的 jar 包
# 4. 重新构建镜像
docker build -t pregnancy-app:1.0.0 .

# 5. 启动新版本
docker compose up -d
```

### 零停机更新（推荐）

```bash
# 使用滚动更新策略
docker compose up -d --no-deps --build app
```

---

## 📞 技术支持

如遇到问题，请提供以下信息：

1. Docker 版本：`docker --version`
2. Docker Compose 版本：`docker compose version`
3. 容器日志：`docker logs pregnancy-app`
4. 系统日志：`journalctl -u docker`

---

**生成时间**: 2026-01-12
**文档版本**: 1.0.0