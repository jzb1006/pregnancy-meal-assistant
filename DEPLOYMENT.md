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

## 🏗️ 架构说明

### 服务架构

```
Internet
    ↓
[Nginx:80] → 反向代理
    ↓
[App:8080] (Docker 内部网络)
    ↓
[MySQL:3306] (Docker 内部网络)
```

### 端口映射

| 服务 | 容器端口 | 宿主机端口 | 说明 |
|------|----------|------------|------|
| Nginx | 80 | 80 | HTTP 访问入口 |
| App | 8080 | - | 内部网络，不直接暴露 |
| MySQL | 3306 | 3306 | 数据库（可外网访问） |

---

## 🚀 快速部署

### 方式一：使用 Docker Compose（推荐）

#### 1. 上传文件到服务器

```bash
# 创建目录
mkdir -p ~/app/pregnancy-meal-assistant
cd ~/app/pregnancy-meal-assistant

# 上传以下文件（整个项目目录）
# - docker-compose.yml
# - Dockerfile
# - .env.example
# - nginx/
# - src/
# - pom.xml
# - 其他项目文件
```

#### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置文件
vim .env
```

**必填配置项：**

```bash
# MySQL 配置
MYSQL_ROOT_PASSWORD=设置强密码
MYSQL_USER=app_user
MYSQL_PASSWORD=设置强密码

# 阿里云 AI
ALI_AI_KEY=your_dashscope_api_key

# 微信小程序
WX_MINIAPP_APPID=your_appid
WX_MINIAPP_SECRET=your_secret

# JWT 密钥
JWT_SECRET=your_very_long_secret_key_at_least_32_chars
```

#### 3. 启动所有服务

```bash
# 构建并启动（会在容器内编译）
docker compose up -d --build

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

#### 4. 验证部署

```bash
# 测试 Nginx 代理
curl http://localhost/api/v3/api-docs

# 测试外网访问（在本地电脑执行）
curl http://203.195.202.54/api/v3/api-docs

# 查看容器健康状态
docker compose ps
```

---

### 方式二：单步部署（适合调试）

#### 1. 构建应用镜像

```bash
cd ~/app/pregnancy-meal-assistant
docker compose build app
```

#### 2. 启动数据库

```bash
docker compose up -d mysql
```

#### 3. 等待数据库就绪

```bash
# 查看数据库日志
docker compose logs -f mysql

# 看到 "ready for connections" 后按 Ctrl+C 退出
```

#### 4. 启动应用

```bash
docker compose up -d app
```

#### 5. 启动 Nginx

```bash
docker compose up -d nginx
```

#### 6. 验证服务

```bash
# 查看所有服务状态
docker compose ps

# 查看应用日志
docker compose logs app | tail -50

# 测试访问
curl http://localhost/api/v3/api-docs
```

---

## 🔧 常用命令

### 容器管理

```bash
# 查看运行中的容器
docker compose ps

# 查看所有服务日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f app
docker compose logs -f nginx
docker compose logs -f mysql

# 重启所有服务
docker compose restart

# 重启特定服务
docker compose restart app

# 停止所有服务
docker compose down

# 停止并删除数据卷（⚠️ 会删除数据）
docker compose down -v
```

### 日志查看

```bash
# 应用日志
docker compose logs app | tail -100

# Nginx 访问日志
docker exec pregnancy-nginx tail -f /var/log/nginx/pregnancy-access.log

# Nginx 错误日志
docker exec pregnancy-nginx tail -f /var/log/nginx/pregnancy-error.log

# MySQL 日志
docker compose logs mysql | tail -100
```

### 进入容器

```bash
# 进入应用容器
docker exec -it pregnancy-app sh

# 进入 Nginx 容器
docker exec -it pregnancy-nginx sh

# 进入 MySQL 容器
docker exec -it pregnancy-mysql mysql -u app_user -p
```

### 数据库操作

```bash
# 连接数据库
docker exec -it pregnancy-mysql mysql -u app_user -p pregnancy_meal

# 备份数据库
docker exec pregnancy-mysql mysqldump -u root -p pregnancy_meal > backup.sql

# 恢复数据库
docker exec -i pregnancy-mysql mysql -u root -p pregnancy_meal < backup.sql
```

---

## 🌐 访问地址

### 服务访问

| 服务 | 内部地址 | 外网地址 |
|------|----------|----------|
| API 接口 | http://localhost/api | http://203.195.202.54/api |
| Swagger 文档 | http://localhost/swagger-ui.html | http://203.195.202.54/swagger-ui.html |
| OpenAPI JSON | http://localhost/api/v3/api-docs | http://203.195.202.54/api/v3/api-docs |
| 数据库 | localhost:3306 | 203.195.202.54:3306 |

### 数据库外网连接

```bash
# 使用 MySQL 客户端连接
mysql -h 203.195.202.54 -P 3306 -u app_user -p

# 或使用工具（Navicat、DBeaver 等）
主机: 203.195.202.54
端口: 3306
用户: app_user
密码: 您设置的密码
数据库: pregnancy_meal
```

---

## 📊 健康检查

### 检查应用状态

```bash
# 检查 OpenAPI 文档
curl http://localhost/api/v3/api-docs

# 检查 Nginx 健康端点
curl http://localhost/health

# 查看容器健康状态
docker inspect --format='{{.State.Health.Status}}' pregnancy-app
docker inspect --format='{{.State.Health.Status}}' pregnancy-nginx
docker inspect --format='{{.State.Health.Status}}' pregnancy-mysql
```

### 检查数据库连接

```bash
# 测试数据库连接
docker exec pregnancy-mysql mysqladmin ping -h 127.0.0.1 -u root -p

# 查看数据库连接数
docker exec pregnancy-mysql mysql -u root -p -e "SHOW PROCESSLIST;"
```

---

## 🔍 故障排查

### 问题 1: 容器启动失败

```bash
# 查看容器日志
docker compose logs app
docker compose logs nginx
docker compose logs mysql

# 查看容器详细信息
docker inspect pregnancy-app
```

### 问题 2: 无法访问应用

```bash
# 检查容器状态
docker compose ps

# 检查 Nginx 配置
docker exec pregnancy-nginx nginx -t

# 重载 Nginx 配置
docker exec pregnancy-nginx nginx -s reload
```

### 问题 3: 数据库连接失败

```bash
# 检查 MySQL 容器
docker compose ps mysql

# 查看 MySQL 日志
docker compose logs mysql

# 测试数据库连接
docker exec -it pregnancy-mysql mysql -u app_user -p
```

### 问题 4: 端口被占用

```bash
# 查看端口占用
sudo netstat -tulpn | grep -E '80|3306'

# 修改 docker-compose.yml 中的端口映射
ports:
  - "8080:80"  # 使用其他端口
```

---

## 🛡️ 安全建议

### 1. 数据库外网访问安全

如果数据库需要外网访问，请务必：

- ✅ 使用强密码（至少 16 位，包含大小写字母、数字、特殊字符）
- ✅ 配置防火墙规则，限制访问 IP
- ✅ 定期更新密码
- ✅ 启用 SSL/TLS 连接

**配置防火墙白名单（Ubuntu）：**

```bash
# 仅允许特定 IP 访问数据库
sudo ufw allow from YOUR_IP_ADDRESS to any port 3306
```

**配置防火墙白名单（CentOS）：**

```bash
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="YOUR_IP_ADDRESS" port protocol="tcp" port="3306" accept'
sudo firewall-cmd --reload
```

### 2. Nginx 安全加固

已启用以下安全措施：

- ✅ 隐藏版本号（server_tokens off）
- ✅ 安全头配置（X-Frame-Options, X-Content-Type-Options 等）
- ✅ 禁止访问隐藏文件

### 3. 应用安全

- ✅ 非 root 用户运行
- ✅ 最小权限原则
- ✅ 健康检查和自动重启

---

## 🔄 更新部署

### 更新应用代码

```bash
# 1. 拉取最新代码
git pull

# 2. 重新构建并启动
docker compose up -d --build app

# 3. 查看日志
docker compose logs -f app
```

### 零停机更新

```bash
# 使用滚动更新
docker compose up -d --no-deps --build app
```

### 更新 Nginx 配置

```bash
# 1. 修改配置文件
vim nginx/conf.d/app.conf

# 2. 测试配置
docker exec pregnancy-nginx nginx -t

# 3. 重载配置
docker exec pregnancy-nginx nginx -s reload
```

---

## 📝 环境变量说明

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| MYSQL_ROOT_PASSWORD | MySQL root 密码 | - | ✅ |
| MYSQL_DATABASE | 数据库名称 | pregnancy_meal | ❌ |
| MYSQL_USER | 应用数据库用户 | app_user | ✅ |
| MYSQL_PASSWORD | 应用数据库密码 | - | ✅ |
| ALI_AI_KEY | 阿里云 DashScope API Key | - | ✅ |
| WX_MINIAPP_APPID | 微信小程序 AppID | - | ✅ |
| WX_MINIAPP_SECRET | 微信小程序 Secret | - | ✅ |
| JWT_SECRET | JWT 签名密钥 | - | ✅ |
| JWT_EXPIRATION | JWT 过期时间(ms) | 604800000 | ❌ |
| JAVA_OPTS | JVM 启动参数 | 默认配置 | ❌ |

---

## 🌍 生产环境建议

### 1. 启用 HTTPS

使用 Let's Encrypt 免费证书：

```bash
# 安装 certbot
sudo apt install certbot

# 生成证书
sudo certbot certonly --standalone -d your-domain.com

# 修改 nginx/conf.d/app.conf，添加 HTTPS 配置
# （参考 nginx/ssl/example.conf）
```

### 2. 配置域名

修改 `nginx/conf.d/app.conf`：

```nginx
server_name your-domain.com;  # 替换为您的域名
```

### 3. 日志管理

```yaml
# 在 docker-compose.yml 中添加
services:
  nginx:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 4. 监控告警

建议使用：
- **Prometheus + Grafana**: 性能监控
- **ELK Stack**: 日志分析
- **Sentry**: 错误追踪

---

## 📞 技术支持

如遇到问题，请提供以下信息：

1. Docker 版本：`docker --version`
2. Docker Compose 版本：`docker compose version`
3. 容器状态：`docker compose ps`
4. 容器日志：`docker compose logs`
5. 系统日志：`journalctl -u docker`

---

**生成时间**: 2026-01-12
**文档版本**: 2.0.0 (包含 Nginx 反向代理)