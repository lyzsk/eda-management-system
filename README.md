# EDA Management System

EDA 工具管理平台，用于登记与监控部署在 Linux / Windows 服务器上的 EDA 工具。

当前是第一阶段壳子：登录、主布局、系统管理（用户/角色/菜单/部门/字典）、EDA 工具列表。

## 技术栈

- Java 17、Spring Boot 3.5.16、Spring Security 6、MyBatis-Plus 3.5.18.1、PostgreSQL 17、Maven 3.9+
- Vue 3、Vite 8、TypeScript、Pinia、Vue Router、Element Plus、SCSS

## 目录

```text
eda-management-system/
├─ ems-common     通用实体、统一响应、异常、Jackson 配置
├─ ems-system     认证、JWT、系统管理、EDA 工具
├─ ems-admin      Spring Boot 启动模块，端口 1115
├─ ems-ui         Vue 3 单页前端，开发端口 5174
└─ sql            每表一个 DDL、幂等 init.sql、seed.sql
```

依赖方向：`ems-admin → ems-system → ems-common`。

## 数据库初始化

```bash
cd /c/Users/admin/dev/eda-management-system

# 1. 创建 ems 角色和数据库（需要 postgres 密码）
PGPASSWORD='<postgres密码>' "/c/Program Files/PostgreSQL/17/bin/psql.exe" \
  -h 127.0.0.1 -U postgres -d postgres -v ON_ERROR_STOP=1 \
  -f sql/create_database.sql

# 2. 建表 + 基础数据
PGPASSWORD='ems' "/c/Program Files/PostgreSQL/17/bin/psql.exe" \
  -h 127.0.0.1 -U ems -d ems -v ON_ERROR_STOP=1 \
  -f sql/init.sql
```

详见 `sql/README.md`。

## 启动后端

```bash
cd /c/Users/admin/dev/eda-management-system
mvn -pl ems-admin -am spring-boot:run
```

或打包后运行：

```bash
mvn -DskipTests package
java -jar ems-admin/target/ems-admin-1.0.jar
```

后端：`http://localhost:1115`

数据库连接默认 `ems/ems`，可通过环境变量覆盖：

```bash
DB_USERNAME=ems
DB_PASSWORD=ems
JWT_SECRET=<至少 32 字节随机字符串>
```

## 启动前端

```bash
cd /c/Users/admin/dev/eda-management-system/ems-ui
npm install
npm run dev
```

前端：`http://localhost:5174`

Vite 代理 `/api` 到 `http://localhost:1115`。

## 默认账号

- 用户名：`admin`
- 密码：`admin123`

## 已实现

- 登录页：左右分屏，无粒子特效、无滑块验证；支持中英文、明亮/暗黑切换
- 主布局：左侧菜单、全屏、中英文、明亮/暗黑、头像菜单
- 仪表盘：用户数、角色数、EDA 工具数、在线工具数、工具状态表
- 系统管理：用户、角色、菜单、部门、字典
- EDA 管理：EDA 工具列表与增删改查

## 后续

- EDA 工具采集脚本与心跳上报
- License 状态探测
- 任务编排 `init() -> task1() -> task2() -> end()`
- 监控历史与告警
