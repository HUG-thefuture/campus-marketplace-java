# Java 后端开发实习生 · 简历项目合集

![CI](https://github.com/HUG-thefuture/campus-marketplace-java/actions/workflows/ci.yml/badge.svg)

本目录包含两个**真实可运行的 Spring Boot 后端工程**，用于应届生投递「Java 后端开发实习生」岗位时作为简历项目，同时也是系统学习 Java 后端核心知识点的配套代码。

| 目录 | 项目 | 定位 | 技术栈重点 |
|------|------|------|-----------|
| `marketplace/` | 校园二手交易平台后端 | **重点，功能完整**（用户/商品/收藏/订单/举报） | Spring Boot 3 + MyBatis + MySQL + JWT + 状态机 + 统一异常 + OpenAPI |
| `shortlink/` | 短链接服务后端 | 精简但自洽（长短转换/跳转/过期/计数） | Spring Boot 3 + MySQL + Base62 短码 + 索引 + 单测 |

---

## 学习指导（必读）

### 1. 建议的学习路径

```
第 1 步：先跑通 shortlink（更简单），建立「Spring Boot 三层架构」的直觉
第 2 步：读 marketplace 的 pom.xml 与 application.yml，理解依赖和配置
第 3 步：从下往上读 marketplace：SQL → entity → mapper → service → controller
第 4 步：重点消化四个横切主题：JWT 、统一异常、订单状态机、分页与索引
第 5 步：把这两套代码的「为什么这么设计」用自己的话复述一遍（面试必问）
```

### 2. 四个横切主题（面试高频，务必吃透）

1. **JWT 鉴权流程**：登录 -> 校验密码 -> 签发 Token -> 前端携带 `Authorization: Bearer <token>` -> 过滤器拦截 -> 校验签名/过期 -> 写入 `SecurityContext` -> 接口读取当前用户。核心是**无状态**，服务端不存 Session，天然适合水平扩展。
2. **统一异常为什么重要**：避免每个 Controller 写一堆 try/catch；用 `@RestControllerAdvice` + `@ExceptionHandler` 把业务异常、参数校验异常、未捕获异常集中映射成统一的 `Result` 结构，前端只需处理一种返回格式。
3. **订单状态机如何防非法流转**：把状态流转规则集中在领域层（`transitionAllowed` 白名单），任何写入都先校验「当前状态 -> 目标状态」是否合法，非法直接抛异常，杜绝 `PENDING -> COMPLETED`、`CANCELED -> PAID` 这类跳过中间态的脏数据。
4. **分页检索与索引**：分页要「先 `COUNT` 再 `LIMIT`」，`LIMIT #{offset}, #{size}` 防止一次拉全表；数据库给 `category`、`keyword` 等检索字段建索引（见 schema.sql），大数据量下避免全表扫描。

### 3. 需要的环境

- JDK 17（Spring Boot 3 最低要求）
- Maven 3.8+
- MySQL 8.x（或直接 `docker compose up -d` 起一个）
- Docker（可选，用于一键起 MySQL）

> 说明：两个工程都附带 Maven Wrapper 说明（见各项目 README），即使本机没装 Maven，只要网络可达，`mvnw` 会自动下载匹配的 Maven 版本。

---

## 快速开始（两个项目通用）

1. 启动 MySQL（三选一）：
   - `docker compose up -d`（在本目录，会起一个 MySQL 8 并初始化两个库）
   - 或本机 MySQL 执行各项目的 `schema.sql` / `data.sql`
   - 或直接修改 `application.yml` 指向已有库
2. 进入项目目录：`cd marketplace` 或 `cd shortlink`
3. 构建：`mvnw clean package`（首次会下载依赖）
4. 启动：`mvnw spring-boot:run`
5. 打开接口文档：`http://localhost:8080/swagger-ui.html`（marketplace）

各项目的详细说明见其子目录下的 `README.md`。

---

## 产品视角（面试可讲）

- **目标用户**：校园二手交易的买家与卖家。
- **解决的问题**：二手信息散落在群里，无订单状态、无举报通道，交易靠自觉。
- **核心场景**：发布→收藏→下单（订单状态机）→举报；JWT 越权防护贯穿。
- **产品入口**：`docker compose up -d` 起库后 Swagger（/swagger-ui.html）全流程可点。
- **成功指标（实测）**：compose 一键起库、全流程接口冒烟通过；shortlink 短链跳转/失效/统计闭环。
- **未来计划**：接口集成测试进 CI（登录 401/越权 403/状态机断言）；图片上传与搜索排序。
