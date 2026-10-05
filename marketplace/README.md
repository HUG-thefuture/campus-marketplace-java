# 校园二手交易平台后端（marketplace）

简历项目一 —— 一个**真实可运行**的 Spring Boot 3 后端工程，覆盖用户、商品、收藏、订单、举报五大模块，重点演示 **JWT 无状态鉴权、统一异常处理、订单状态机、分页检索与索引、越权防护** 等面试核心知识点。

---

## 一、技术栈

| 技术 | 用途 |
|------|------|
| Spring Boot 3.2.5 | 主框架 |
| MyBatis 3.0.3 | 持久层（SQL 写在 XML，Join 更灵活） |
| MySQL 8 | 数据库 |
| JWT (jjwt 0.12.5) | 无状态鉴权 |
| springdoc-openapi 2.5.0 | 接口文档（Swagger UI） |
| Spring Validation | 参数校验（@Valid） |
| BCrypt | 密码哈希（复用 spring-security-crypto） |

---

## 二、如何构建与运行

### 0. 前置环境
- JDK 17+
- MySQL 8（或使用项目根目录的 `docker compose up -d` 一键启动）
- Maven 3.8+（无 Maven 时用 Wrapper，见下）

### 1. 初始化数据库
两种方式任选：

**方式 A：Docker（推荐）** —— 在项目根目录 `project/` 下执行：
```bash
docker compose up -d
```
它会启动 MySQL 8 并依次执行本项目的 `schema.sql` + `data.sql`，自动建库、建表、灌示例数据。

**方式 B：手动** —— 登录 MySQL 后执行：
```sql
CREATE DATABASE IF NOT EXISTS marketplace DEFAULT CHARSET utf8mb4;
USE marketplace;
SOURCE src/main/resources/schema.sql;
SOURCE src/main/resources/data.sql;
```

### 2. 配置数据库连接
编辑 `src/main/resources/application.yml`，确认 `spring.datasource.url/username/password` 与你本地 MySQL 一致（默认 `root/root`）。

### 3. 构建与启动
```bash
# 使用 Maven Wrapper（本机无需预装 Maven，首次会自动下载）
./mvnw clean package          # Windows 用 mvnw.cmd

# 或直接运行
./mvnw spring-boot:run        # Windows 用 mvnw.cmd
```
> 提示：本机若未配置 `mvnw`，可先执行 `mvn -N wrapper:wrapper` 生成；也可直接 `mvn clean package spring-boot:run`。

启动成功后访问 Swagger 文档：
- 接口文档：<http://localhost:8080/swagger-ui.html>
- OpenAPI JSON：<http://localhost:8080/v3/api-docs>

### 4. 示例账号
| 用户名 | 密码 | 角色 |
|--------|------|------|
| `user01` | `123456` | 普通用户 |
| `admin`  | `123456` | 管理员 |

> 注意：示例账号的密码由应用启动时的 `DataInitializer` 用 BCrypt **现场生成正确哈希**（而非在 SQL 里硬编码），
> 这样能保证 `123456` 一定可以登录，也演示了「启动时初始化管理员账号」这一真实项目常见做法。

### 5. 无 Docker 的本机复现方案

本机已装 MySQL 8（如 MySQL 8.4 服务版）时，无需 Docker，完整复现命令序列如下（Git Bash 下验证通过）：

```bash
# 0) 确认 MySQL 已启动，root 可登录（本机默认 root/root，见 application.yml）

# 1) 建库：schema.sql / data.sql 内只有 USE marketplace，不含 CREATE DATABASE，必须先建
mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS marketplace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2) 建表 + 灌示例数据（与 docker compose 挂载的是同一份脚本）
cd marketplace
mysql --default-character-set=utf8mb4 -uroot -proot marketplace < src/main/resources/schema.sql
mysql --default-character-set=utf8mb4 -uroot -proot marketplace < src/main/resources/data.sql

# 3) 编译并启动（无预装 Maven 也能跑，Wrapper 首次会自动下载 Maven 发行版）
./mvnw spring-boot:run          # Windows CMD / PowerShell 用：mvnw.cmd spring-boot:run

# 4) 验证（另开终端）
#    Swagger：http://localhost:8080/swagger-ui.html
curl -s -X POST http://localhost:8080/api/user/login \
     -H "Content-Type: application/json" \
     -d '{"username":"user01","password":"123456"}'     # 返回 code:200 与 JWT 即成功
```

说明：
- **Windows 下第 2 步必须带 `--default-character-set=utf8mb4`**：mysql 客户端默认用系统字符集（GBK）解析 UTF-8 脚本，会把表注释/中文数据导成乱码甚至报 `Incorrect string value` 错误。
- 第 2 步也可跳过：`application.yml` 里 `spring.sql.init.mode: always` 会在应用首次启动时自动执行同一对脚本（均为幂等的 `IF NOT EXISTS` / `ON DUPLICATE KEY`），但**第 1 步建库不能省**（否则数据源连不上）。
- 示例账号 user01 / admin 由应用启动时 `DataInitializer` 自动创建，无需手动插入。
- 若本机 root 密码与 `application.yml`（默认 `root`）不同，改 yml 或临时用环境变量覆盖：`SPRING_DATASOURCE_PASSWORD=你的密码 ./mvnw spring-boot:run`（PowerShell 先 `$env:SPRING_DATASOURCE_PASSWORD="你的密码"`）。

---

## 三、接口清单

统一响应结构 `Result<T>`：`{ code, message, data }`
- `code`：200 成功 / 400 参数或业务错误 / 401 未登录 / 403 无权限 / 404 资源不存在 / 500 服务器错误

### 用户模块
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/user/register` | 注册 | 无 |
| POST | `/api/user/login` | 登录，返回 JWT | 无 |
| GET  | `/api/user/me` | 当前登录用户信息 | 需登录 |

### 商品模块
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/product` | 发布商品 | 需登录 |
| POST | `/api/product/{id}/off-shelf` | 下架商品 | 需卖家本人 |
| GET  | `/api/product/{id}` | 商品详情 | 无 |
| GET  | `/api/product/search?keyword=&category=&page=&size=` | 分页检索 | 无 |
| GET  | `/api/product/mine` | 我的商品 | 需登录 |

### 收藏模块
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST   | `/api/favorite/{productId}` | 收藏商品 | 需登录 |
| DELETE | `/api/favorite/{productId}` | 取消收藏 | 需登录 |
| GET    | `/api/favorite/{productId}/status` | 是否已收藏 | 需登录 |

### 订单模块（状态机演示）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/order` | 创建订单（PENDING） | 需登录 |
| POST | `/api/order/{id}/status` | 状态流转 | 买/卖家 |
| GET  | `/api/order/{id}` | 订单详情 | 买/卖家 |
| GET  | `/api/order/bought` | 我买到的 | 需登录 |
| GET  | `/api/order/sold` | 我卖出的 | 需登录 |

### 举报模块
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/report` | 举报商品 | 需登录 |
| GET  | `/api/report/mine` | 我发起的举报 | 需登录 |
| GET  | `/api/report` | 全部举报 | 仅管理员 |
| POST | `/api/report/{id}/handle` | 处理举报 | 仅管理员 |

---

## 四、为什么要这么设计（学习要点，面试必问）

### 1. JWT 鉴权流程
```
登录 -> 校验密码(BCrypt.matches) -> 签发 token(封装 userId + role)
 -> 前端带头 Authorization: Bearer <token>
 -> JwtAuthFilter 拦截 -> 解析并校验签名/过期
 -> 写入 UserContext(ThreadLocal) -> 接口读取当前登录人
```
- **为什么无状态？** 服务端不存 Session，任意节点都能独立校验，方便水平扩展。
- **请求结束为什么要 clear ThreadLocal？** 线程池复用会串号，必须清理防止内存泄漏。
- **密钥管理（2026-09 安全加固）**：`jwt.secret` 不再写死在仓库——未配置 `JWT_SECRET` 时启动随机生成（重启后旧 token 全部失效，对演示/测试无损）；生产环境通过环境变量注入固定密钥（HS256 要求至少 32 字节，过短会在启动时报错提示）。历史提交中出现过的旧密钥一律视为已泄露，禁止复用。

### 2. 统一异常为什么重要
- 避免每个 Controller 写满 try/catch，`@RestControllerAdvice` 集中把「业务异常 / 参数校验异常 / 系统异常」映射成统一结构。
- 前端只需处理一种返回格式，且不会拿到堆栈、SQL 等敏感信息。

### 3. 订单状态机如何防非法流转
- 把流转规则内聚在 `OrderStatus` 枚举的 `canTransitTo` 里：`PENDING -> PAID -> COMPLETED`，`PENDING/PAID -> CANCELED`，终态不可再流转。
- 两层防护：① 业务层先校验白名单；② 数据库层用 `UPDATE ... WHERE status = expectedFrom` 做乐观锁，防并发脏写。

### 4. 分页检索与索引
- 分页正确姿势：先 `COUNT` 得总数，再 `LIMIT #{offset}, #{size}` 取当前页，避免一次拉全表。
- `schema.sql` 给 `category`、`status`、`seller_id` 建了 B-tree 索引（等值/前缀匹配有效）；
- **诚实口径（2026-09 审计修正）**：商品检索用的是 `LIKE '%关键词%'`，前置通配符会让 B-tree 索引失效、必然全表扫描——`idx_product_title` 只对 `LIKE '关键词%'` 前缀匹配有用。数据量大时的正确方案是 FULLTEXT 全文索引（MySQL 8 的 ngram 分词）或搜索引擎（ES）。

### 5. 越权防护（返回 403）
- 商品下架、订单操作、订单详情都校验「操作人是否为资源 owner」，不是本人直接抛 403。
- 管理员用 `@RequireRole(RoleEnum.ADMIN)` 注解 + `RoleInterceptor` 拦截器做角色校验。

---

## 五、代码结构

```
marketplace/
├── pom.xml
└── src/main/
    ├── java/com/resume/marketplace/
    │   ├── MarketplaceApplication.java      # 启动类
    │   ├── annotation/RequireRole.java      # 角色注解
    │   ├── common/                           # Result / PageResult / 异常
    │   ├── config/                           # Web安全 / OpenAPI
    │   ├── controller/                       # 5 个控制器
    │   ├── dto/                              # 请求/响应对象
    │   ├── entity/                           # 实体
    │   ├── enums/                            # 角色/商品/订单/举报枚举
    │   ├── mapper/                           # MyBatis 接口
    │   ├── security/                         # JWT / 过滤器 / 拦截器
    │   └── service/                          # 服务接口 + 实现
    └── resources/
        ├── application.yml
        ├── schema.sql
        ├── data.sql
        └── mapper/*.xml                      # SQL
```