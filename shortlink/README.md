# 短链接服务后端（shortlink）

简历项目二 —— 一个精简但**完整自洽**的短链接服务，演示「长链转短码、短链跳转、过期失效、访问计数」的核心实现，并用单元测试自测关键逻辑。

---

## 一、技术栈

| 技术 | 用途 |
|------|------|
| Spring Boot 3.2.5 | 主框架 |
| Spring JDBC（JdbcTemplate） | 持久层（轻量，演示不依赖 ORM 的写法） |
| MySQL 8 | 数据存储 |
| JUnit 5 | 单元测试（Base62 编解码自测） |
| Spring Validation | 参数校验 |

> 与 marketplace 用了 MyBatis 不同，这里刻意用 JdbcTemplate，展示两种持久层方案的差异，面试时可对比说明。

---

## 二、如何构建与运行

### 0. 前置
- JDK 17+、MySQL 8（或项目根目录 `docker compose up -d`）、Maven 3.8+

### 1. 初始化数据库
```sql
CREATE DATABASE IF NOT EXISTS shortlink DEFAULT CHARSET utf8mb4;
USE shortlink;
SOURCE src/main/resources/schema.sql;
```
（若用根目录 `docker compose up -d`，会自动执行 `schema.sql`。）

### 2. 配置连接
修改 `src/main/resources/application.yml` 的 `spring.datasource.*`（默认 `root/root`）。

### 3. 构建与启动
```bash
./mvnw clean package          # Windows: mvnw.cmd
./mvnw spring-boot:run        # Windows: mvnw.cmd
```
> 本机无 Maven 时，先执行 `mvn -N wrapper:wrapper` 生成 Wrapper；或直接 `mvn clean package spring-boot:run`。

端口为 **8081**（避免与 marketplace 的 8080 冲突）。

### 4. 运行测试
```bash
./mvnw test       # 运行 Base62 编解码等单元测试
```

---

## 三、接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/shorten` | 长链转短链，返回短码 + 完整短链 |
| GET  | `/{code}` | 短链跳转（302 重定向到原始长链） |

### 请求示例
```bash
# 1. 创建短链
curl -X POST http://localhost:8081/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com/some/very/long/path?x=1&y=2"}'

# 响应：
# { "code":200, "message":"success", "data": { "shortCode":"aB3xYz", "shortUrl":"http://localhost:8081/aB3xYz", ... } }

# 2. 访问短链（浏览器打开或 curl -L 跟随跳转）
curl -L http://localhost:8081/aB3xYz
```

### 请求体字段（POST /api/shorten）
```json
{
  "originalUrl": "https://example.com/...",   // 必填，须以 http(s):// 开头
  "customCode": "mylink",                     // 可选，自定义短码（占用则报错）
  "expireMs": 86400000                        // 可选，有效期毫秒，缺省 30 天
}
```

---

## 四、为什么要这么设计（学习要点）

### 1. 短码生成：随机 + Base62 编码
- 用 SecureRandom 生成随机数，再 Base62 编码成「只含 0-9a-zA-Z」的短码。
- **为什么 Base62？** 62 个字符不含 `+/=` 等 URL 不友好符号；62^6 ≈ 568 亿，6 位足够。
- **唯一性怎么保证？** 数据库 `short_code` 建了唯一索引兜底，应用层再对冲突做「重试」。

### 2. 跳转用 302 还是 301？
- 短链用 **302（临时重定向）**：跳转目标可能变、需要统计访问量，301 会被浏览器/代理永久缓存导致后续改链不生效。

### 3. 访问计数的并发安全
- 用 `UPDATE ... SET access_count = access_count + 1` 的**原子自增**，避免「先读再写」在高并发下计数丢失。

### 4. 过期与索引
- `expire_at` 存失效时间，`resolve` 时比较当前时间判断过期（返回 410）。
- `expire_at` 建了索引，方便定时任务批量清理过期短链时快速定位。

### 5. 为什么这里用 JdbcTemplate 而不用 MyBatis
- JdbcTemplate 对单表简单 CRUD 更轻量，SQL 就近写在 Repository，语义直白；
- MyBatis 更适合复杂 Join、动态 SQL 多的场景。两种方案都会写，面试是加分项。

---

## 五、代码结构

```
shortlink/
├── pom.xml
└── src/
    ├── main/java/com/resume/shortlink/
    │   ├── ShortlinkApplication.java
    │   ├── common/          # Result / BusinessException / 全局异常处理
    │   ├── controller/      # ShortUrlController
    │   ├── dto/             # 请求/响应对象
    │   ├── entity/          # ShortUrl 实体
    │   ├── repository/      # JdbcTemplate 数据访问
    │   ├── service/         # 服务接口 + 实现
    │   └── util/            # Base62 编解码
    ├── main/resources/
    │   ├── application.yml
    │   └── schema.sql
    └── test/java/.../util/Base62Test.java
```