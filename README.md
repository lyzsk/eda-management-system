# EDA Management System

EDA 工具管理平台，用于登记与监控部署在 Linux / Windows 服务器上的 EDA 工具。

当前是第一阶段壳子：登录、主布局、系统管理（用户 / 角色 / 菜单 / 部门 / 字典）、EDA 工具列表。

## 技术栈

- 后端：Java 17, Spring Boot 3.5.16, Spring Security 6, MyBatis-Plus 3.5.18.1, PostgreSQL 17, Maven 3.9+
- 前端：Vue 3, Vite 8, TypeScript, Pinia, Vue Router, Element Plus, SCSS

## 环境要求

| 组件       | 版本               | 说明                           |
| :--------- | :----------------- | :----------------------------- |
| JDK        | 17                 | 编译与运行                     |
| Maven      | 3.9+               | 多模块构建                     |
| Node.js    | 20+（本机实测 24） | 前端构建                       |
| npm        | 10+（本机实测 11） | 前端依赖                       |
| PostgreSQL | 17                 | 默认数据库 `ems`、schema `ems` |

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

`psql` 不在该路径时，把它加入 `PATH` 后直接执行 `psql ...` 即可。详见 `sql/README.md`。

## 后端配置

配置在 `ems-admin/src/main/resources/application.yml`，均可用环境变量覆盖：

| 环境变量                 | 默认值（仅开发）                                         | 说明                                                                |
| :----------------------- | :------------------------------------------------------- | :------------------------------------------------------------------ |
| `DB_URL`                 | `jdbc:postgresql://localhost:5432/ems?currentSchema=ems` | JDBC 连接串                                                         |
| `DB_USERNAME`            | `ems`                                                    | 数据库账号                                                          |
| `DB_PASSWORD`            | `ems`                                                    | 数据库密码                                                          |
| `JWT_SECRET`             | `ems-dev-only-secret-0123456789abcdef`                   | JWT 签名密钥，**生产必须替换**（至少 32 字节随机串）                |
| `JWT_TTL_MINUTES`        | `480`                                                    | access token 有效期（分钟）                                         |
| `EMS_SSH_KEY_FILE`       | 空                                                       | SSH 私钥文件路径（推荐，权限 600）；与 `EMS_SSH_PRIVATE_KEY` 二选一 |
| `EMS_SSH_PRIVATE_KEY`    | 空                                                       | SSH 私钥内容（多行 PEM），优先于文件                                |
| `EMS_SSH_KEY_PASSPHRASE` | 空                                                       | 私钥口令，可空                                                      |
| `EMS_SSH_USER`           | 空                                                       | 全局默认 SSH 账号；服务器未单独填写账号时使用                       |

> 默认密码与默认 JWT 密钥只用于本地开发，部署前务必用环境变量覆盖。SSH 私钥只来自部署配置，不要提交到仓库或写入数据库。

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

## 启动前端

```bash
cd /c/Users/admin/dev/eda-management-system/ems-ui
npm install
npm run dev      # 开发：http://localhost:5174
npm run build    # 生产构建：产物在 ems-ui/dist
```

Vite 开发代理 `/api` → `http://localhost:1115`（见 `ems-ui/.env.development`）。
生产部署时把 `dist/` 交给 Nginx 等静态服务器，并将 `/api` 反向代理到后端；前端的 `VITE_API_PROXY` 仅开发服务器使用。

## API 约定

- 后端接口统一前缀 `/api`：`/api/auth/*`、`/api/system/*`、`/api/eda/tools`、`/api/eda/servers`、`/api/eda/scripts`。
- 响应体统一为 `Result<T>`：`{ "code": "...", "msg": "...", "data": ... }`。
- 认证使用 JWT（`Authorization: Bearer <token>`），登录接口 `/api/auth/login`。

## 服务器与脚本执行

- `eda_server` 只登记 SSH 目标元数据（host/端口/系统/账号）；**私钥不进数据库**，由部署配置 `EMS_SSH_KEY_FILE`（或 `EMS_SSH_PRIVATE_KEY`）统一提供，公钥由运维放到各服务器的 `authorized_keys`。
- 默认用一把部署密钥登录所有 EDA 服务器；个别机器需要不同账号时，在服务器记录里单独填写账号（不填则用 `EMS_SSH_USER`）。
- `eda_tool` 通过 `server_id` 关联服务器；未关联服务器的工具不能执行脚本。
- `eda_script` 按工具登记 START/STOP/RESTART/STATUS/TASK 脚本，命令由管理员维护。
- 执行走异步任务：`POST /api/eda/scripts/{id}/run` 返回 `runId`，前端轮询 `GET /api/eda/scripts/runs/{runId}`；每次执行都写入 `eda_script_run` 审计历史。
- 骨架阶段 SSH 首次连接信任主机指纹；生产环境应改为 known_hosts 校验。Windows 目标需启用 OpenSSH Server（否则后续接 WinRM/Agent 执行器）。

## 默认账号

- 用户名：`admin`
- 密码：`admin123`

## 已实现

- 登录页：左右分屏，无粒子特效、无滑块验证；支持中英文、明亮/暗黑切换
- 主布局：左侧菜单、全屏、中英文、明亮/暗黑、头像菜单
- 仪表盘：用户数、角色数、EDA 工具数、在线工具数、工具状态表
- 系统管理：用户、角色、菜单、部门、字典
- EDA 管理：EDA 工具列表与增删改查
- 服务器管理：SSH 目标增删改查、凭据加密保存、连接测试
- 脚本管理：按 EDA 工具登记启动/停止/重启/状态/任务脚本，异步执行、轮询结果、执行历史

## 后续

- EDA 工具采集脚本与心跳上报
- License 状态探测
- 任务编排 `init() -> task1() -> task2() -> end()`
- 监控历史与告警
