# Linux 开发环境搭建指南

本文档说明如何在 Linux（Ubuntu/Debian 和 CentOS/RHEL/Rocky）系统上搭建 Course Buddy Backend 本地开发环境。

---

## 支持的发行版

| 发行版 | 版本 | 测试状态 |
|--------|------|----------|
| Ubuntu | 22.04 LTS (Jammy) | ✅ 推荐 |
| Ubuntu | 20.04 LTS (Focal) | ✅ |
| Debian | 11 (Bullseye) / 12 (Bookworm) | ✅ |
| CentOS Stream | 9 | ✅ |
| Rocky Linux | 9 | ✅ |
| RHEL | 9 | ✅ |

---

## 第一步：安装 JDK 17

### Ubuntu / Debian

```bash
# 方式一：使用系统包管理器（最简单）
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

# 方式二：使用 SDKMAN（推荐，支持多版本切换）
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.13-tem
sdk default java 17.0.13-tem

# 方式三：使用 Adoptium 官方 apt 仓库
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /etc/apt/trusted.gpg.d/adoptium.asc
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt-get update
sudo apt-get install -y temurin-17-jdk
```

### CentOS / Rocky Linux / RHEL

```bash
# 方式一：使用系统包管理器
sudo dnf install -y java-17-openjdk-devel

# 方式二：使用 SDKMAN（同上）
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.13-tem
```

### 配置 JAVA_HOME

```bash
# 查找 Java 安装路径
readlink -f $(which java) | sed "s|/bin/java||"
# 示例：/usr/lib/jvm/temurin-17-amd64

# 添加到 ~/.bashrc 或 ~/.zshrc
echo 'export JAVA_HOME=/usr/lib/jvm/temurin-17-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 验证
java -version
# openjdk version "17.x.x" ...
javac -version
# javac 17.x.x
```

---

## 第二步：安装 Git

### Ubuntu / Debian

```bash
sudo apt-get install -y git

# 验证
git --version

# 配置
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global core.autocrlf input
```

### CentOS / Rocky / RHEL

```bash
sudo dnf install -y git
git --version
```

---

## 第三步：安装 MySQL 8.0

### Ubuntu 22.04

```bash
# 安装 MySQL 8.0
sudo apt-get install -y mysql-server

# 启动并设置开机自启
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置（设置 root 密码等）
sudo mysql_secure_installation

# 验证
sudo systemctl status mysql
mysql --version
# mysql  Ver 8.x.x
```

### Ubuntu 20.04 / Debian（如系统默认版本较旧）

```bash
# 添加 MySQL 官方仓库
wget https://dev.mysql.com/get/mysql-apt-config_0.8.29-1_all.deb
sudo dpkg -i mysql-apt-config_0.8.29-1_all.deb
# 在弹出界面中选择 MySQL 8.0

sudo apt-get update
sudo apt-get install -y mysql-server
```

### CentOS / Rocky / RHEL 9

```bash
# 添加 MySQL 官方仓库
sudo dnf install -y https://dev.mysql.com/get/mysql80-community-release-el9-1.noarch.rpm
sudo dnf install -y mysql-community-server

# 启动
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 获取临时 root 密码
sudo grep 'temporary password' /var/log/mysqld.log

# 修改密码
mysql -uroot -p
# 输入临时密码后执行：
# ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword@123';
```

### 创建开发数据库

```bash
# 连接 MySQL
mysql -u root -p

# 执行以下 SQL
```

```sql
CREATE DATABASE IF NOT EXISTS course_buddy
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'course_buddy_user'@'localhost'
    IDENTIFIED BY 'Dev@123456';

GRANT ALL PRIVILEGES ON course_buddy.* TO 'course_buddy_user'@'localhost';
FLUSH PRIVILEGES;

-- 验证
SHOW DATABASES LIKE 'course_buddy';
SELECT user, host FROM mysql.user WHERE user = 'course_buddy_user';
```

---

## 第四步：安装 Redis 7

### Ubuntu / Debian

```bash
# 方式一：使用官方 APT 仓库（推荐，获取最新版）
curl -fsSL https://packages.redis.io/gpg | sudo gpg --dearmor -o /usr/share/keyrings/redis-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/redis-archive-keyring.gpg] https://packages.redis.io/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/redis.list
sudo apt-get update
sudo apt-get install -y redis

# 方式二：使用系统包（版本可能较旧）
sudo apt-get install -y redis-server

# 启动服务
sudo systemctl start redis-server
sudo systemctl enable redis-server

# 验证
redis-cli ping
# PONG
redis-cli --version
```

### CentOS / Rocky / RHEL 9

```bash
# 启用 EPEL 仓库
sudo dnf install -y epel-release

# 安装 Redis
sudo dnf install -y redis

# 或者使用 Remi 仓库获取更新版本
sudo dnf install -y https://rpms.remirepo.net/enterprise/remi-release-9.rpm
sudo dnf module enable redis:remi-7.2 -y
sudo dnf install -y redis

# 启动
sudo systemctl start redis
sudo systemctl enable redis

# 验证
redis-cli ping
```

---

## 第五步：安装 Maven（可选）

```bash
# Ubuntu / Debian
sudo apt-get install -y maven

# CentOS / Rocky / RHEL
sudo dnf install -y maven

# 验证
mvn -version
# Apache Maven 3.x.x
```

项目自带 `./mvnw`，也可以直接使用 Maven Wrapper，无需单独安装。

---

## 第六步：克隆并配置项目

```bash
# 克隆仓库
git clone <repository-url>
cd course-buddy-backend

# 赋予 Maven Wrapper 执行权限
chmod +x mvnw
```

### 配置开发环境

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_buddy?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: course_buddy_user
    password: Dev@123456     # 替换为你设置的密码
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 5
  data:
    redis:
      host: localhost
      port: 6379
  flyway:
    enabled: true

logging:
  level:
    root: INFO
    com.coursebuddy: DEBUG
    org.springframework.security: DEBUG
```

---

## 第七步：运行项目

### 方式一：命令行运行（无需 IDE）

```bash
# 设置环境变量并启动
export JWT_SECRET="dev-local-secret-key-at-least-32-characters-long"

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

等待看到如下日志，表示启动成功：

```
Started CourseBuddyApplication in X.XXX seconds (process running for X.XXX)
```

### 方式二：构建 JAR 后运行

```bash
# 构建
./mvnw package -DskipTests

# 运行
java -jar target/course-buddy-backend-1.0.0.jar \
  --spring.profiles.active=dev \
  --jwt.secret=dev-local-secret-key-at-least-32-chars
```

### 验证启动

```bash
curl http://localhost:8080/api/actuator/health
# {"status":"UP"}

# 完整健康检查
curl -s http://localhost:8080/api/actuator/health | python3 -m json.tool
```

---

## 第八步：配置 IDE（可选）

### IntelliJ IDEA（推荐）

```bash
# 下载 IDEA 社区版（免费）
wget https://download.jetbrains.com/idea/ideaIC-2023.3.tar.gz
tar -xzf ideaIC-2023.3.tar.gz
cd idea-IC-*/bin
./idea.sh
```

或者通过 JetBrains Toolbox：

```bash
# 下载并安装 JetBrains Toolbox
wget -O jetbrains-toolbox.tar.gz "https://data.services.jetbrains.com/products/download?platform=linux&code=TBA"
# 解压并运行
```

### VS Code（轻量选择）

```bash
# Ubuntu
sudo snap install --classic code

# 安装 Java 扩展包
code --install-extension vscjava.vscode-java-pack
code --install-extension redhat.vscode-xml
```

---

## 防火墙配置

开发环境通常不需要配置防火墙，但如果是在服务器上开发并需要外部访问：

```bash
# Ubuntu (ufw)
sudo ufw allow 8080/tcp

# CentOS/Rocky (firewalld)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

---

## 配置 Maven 国内镜像

```bash
mkdir -p ~/.m2
cat > ~/.m2/settings.xml << 'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF
```

---

## 常见问题

### 问题 1：MySQL 启动失败

```bash
# 查看错误日志
sudo journalctl -u mysql -n 50

# 常见原因：数据目录权限问题
sudo chown -R mysql:mysql /var/lib/mysql
sudo systemctl restart mysql
```

### 问题 2：Redis 绑定地址问题

默认 Redis 只监听 127.0.0.1，这是正确的开发配置。如需远程访问：

```bash
# 编辑 Redis 配置（谨慎操作，仅在安全网络中）
sudo vim /etc/redis/redis.conf
# 找到 bind 行，改为：
# bind 0.0.0.0

sudo systemctl restart redis-server
```

### 问题 3：端口被占用

```bash
# 查找占用 8080 的进程
sudo lsof -i :8080
# 或
sudo ss -tlnp | grep 8080

# 停止进程
kill -15 <PID>
```

### 问题 4：权限不足

```bash
# Maven 本地仓库权限
sudo chown -R $USER:$USER ~/.m2

# 项目目录权限
sudo chown -R $USER:$USER ~/course-buddy-backend
```

### 问题 5：Flyway 迁移错误

```bash
# 开发环境重置 Flyway（会重新执行所有迁移）
mysql -u course_buddy_user -p course_buddy -e "DROP TABLE IF EXISTS flyway_schema_history;"
# 然后重启应用
```

---

## 下一步

- 阅读 [Java 代码规范](../../代码规范/Java代码规范.md)
- 了解 [Docker 本地开发环境](./Docker本地开发环境.md)（可与本地开发结合使用）
- 查阅 [API 端点概览](../../../技术文档/API文档/API端点概览.md)
