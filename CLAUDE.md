# 智能健康饮食 — 后端 (Health Diet Backend)

Spring Boot 2.7.18 智能饮食管理后端，提供用户认证、饮食记录、营养统计、AI 分析等 REST API。

## 技术栈

- **框架**: Spring Boot 2.7.18 + Spring Security + JWT
- **ORM**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 5.7.44（D 盘 Program Files）
- **AI**: DeepSeek API（deepseek-chat / deepseek-reasoner）
- **构建**: Maven + Java 17（OpenJDK AdoptOpenJDK-17）
- **工具**: Lombok、Hutool、Jackson、BCryptPasswordEncoder

## 目录结构

```
health-diet-backend/
├── pom.xml
└── src/main/
    ├── java/com/health/
    │   ├── HealthApplication.java          # 主入口
    │   ├── common/                          # Result, PageResult, BusinessException
    │   ├── config/                          # 配置类
    │   │   ├── SecurityConfig.java          # Spring Security + JWT 过滤器链
    │   │   ├── CorsConfig.java              # 全局 CORS（允许所有源）
    │   │   ├── FileUploadConfig.java        # 静态资源映射 /uploads/**
    │   │   └── MybatisPlusConfig.java       # 分页插件
    │   ├── controller/                      # 控制器（5 个）
    │   │   ├── AuthController.java          # /api/auth 注册/登录
    │   │   ├── UserController.java          # /api/user 个人中心
    │   │   ├── FoodRecordController.java    # /api/food/record 饮食记录
    │   │   ├── StatsController.java         # /api/stats 数据统计
    │   │   └── AiController.java            # /api/ai AI 分析
    │   ├── dto/                             # 请求 DTO（7 个）
    │   ├── entity/                          # 实体类（3 个）
    │   │   ├── User.java                    # @TableName("user")
    │   │   ├── FoodRecord.java              # @TableName("food_record")
    │   │   └── AiAnalysisLog.java           # @TableName("ai_analysis_log")
    │   ├── handler/
    │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
    │   │   └── JwtAuthenticationTokenFilter.java  # JWT 认证过滤器
    │   ├── mapper/                          # MyBatis-Plus Mapper
    │   │   ├── UserMapper.java
    │   │   ├── FoodRecordMapper.java
    │   │   └── AiAnalysisLogMapper.java
    │   ├── service/                         # 服务层（4 个接口 + 4 个实现）
    │   │   ├── UserService / UserServiceImpl
    │   │   ├── FoodRecordService / FoodRecordServiceImpl
    │   │   ├── StatsService / StatsServiceImpl
    │   │   └── AiNutritionService / AiNutritionServiceImpl
    │   ├── utils/
    │   │   └── JwtUtils.java                # JWT 生成/解析
    │   └── vo/                              # 响应 VO（6 个）
    └── resources/
        ├── application.yml                  # 主配置
        └── db/init.sql                      # 数据库初始化脚本
```

## API 接口一览（17 个端点）

### 公开接口（无需 Token）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录，返回 JWT Token |
| GET | `/uploads/**` | 静态文件（头像等） |

### 用户管理 `/api/user`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user/profile` | 获取个人资料 |
| PUT | `/api/user/profile` | 更新资料（身高/体重/目标等） |
| PUT | `/api/user/password` | 修改密码 |
| POST | `/api/user/avatar` | 上传头像（multipart） |
| POST | `/api/user/avatar/base64` | 上传头像（Base64） |

### 饮食记录 `/api/food/record`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/food/record` | 新增记录 |
| GET | `/api/food/record/list?date=...` | 按日期查询（含当日营养汇总） |
| PUT | `/api/food/record/{id}` | 修改记录 |
| DELETE | `/api/food/record/{id}` | 删除记录 |

### 数据统计 `/api/stats`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/stats/calorie-trend?range=7` | 卡路里趋势（7/30/90 天） |
| GET | `/api/stats/nutrient-ratio?date=...` | 营养素比例（蛋白质/碳水/脂肪） |

### AI 分析 `/api/ai`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/ai/analyze/manual` | 手动输入食物分析（调用 DeepSeek） |
| POST | `/api/ai/analyze/diet` | 全日饮食分析（调用 DeepSeek） |
| GET | `/api/ai/profile-tip` | 个性化饮食建议 |
| GET | `/api/ai/analyze/history?page=&pageSize=` | 分析历史（分页） |
| DELETE | `/api/ai/analyze/{id}` | 删除单条分析 |
| DELETE | `/api/ai/analyze/history` | 清空分析历史 |

## 数据库

### 连接信息
- **地址**: `localhost:3306`
- **数据库**: `health_db`
- **用户**: `root`
- **密码**: `<你的数据库密码>`（通过 `DB_PASSWORD` 环境变量传入）
- **Windows 服务名**: `MySQL57`（开机自启）

### 数据表（4 张）

| 表名 | 说明 | 关键字段 |
|---|---|---|
| `user` | 用户表 | id, username, password(BCrypt), height, weight, goal, gender, avatar, age |
| `food_record` | 饮食记录 | id, user_id, food_name, calories, protein, carbs, fat, meal_type, record_date |
| `ai_analysis_log` | AI 分析日志 | id, user_id, food_name, analysis_type, calories, protein, carbs, fat, advice, details |
| `daily_nutrition_summary` | 每日营养汇总 | (MyBatis-Plus 查询聚合生成) |

## 配置与环境变量

```bash
# 必需环境变量
export DB_PASSWORD=<你的数据库密码>
export JWT_SECRET=<你的JWT密钥>
export DEEPSEEK_API_KEY=<你的DeepSeek API Key>
```

### application.yml 关键配置

| 配置项 | 值 | 说明 |
|---|---|---|
| `server.port` | 8080 | 服务端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/health_db` | 数据库连接 |
| `spring.datasource.password` | `${DB_PASSWORD:}` | 从环境变量读取 |
| `jwt.secret` | `${JWT_SECRET:}` | Base64 编码密钥 |
| `jwt.expiration` | 604800000（7 天） | Token 过期时间 |
| `deepseek.api-key` | `${DEEPSEEK_API_KEY:}` | DeepSeek API Key |
| `deepseek.model` | deepseek-chat | AI 模型 |
| `file.upload-path` | ./uploads | 文件上传目录 |

> **注意**: JWT secret 必须是 **Base64 编码**字符串才能正确解码。如果未配置 DeepSeek Key，AI 接口自动回退到 Mock 数据。

## 启动方式

```bash
# 1. 确保 MySQL57 服务已启动
# 2. 设置环境变量
export DB_PASSWORD=<你的数据库密码>
export JWT_SECRET=<你的JWT密钥>
export DEEPSEEK_API_KEY=<你的DeepSeek API Key>

# 3. 启动
cd health-diet-backend
mvn spring-boot:run
```

启动成功标志：
```
Started HealthApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

## 安全机制

- **认证**: JWT Bearer Token（7 天有效）
- **密码加密**: BCryptPasswordEncoder
- **过滤链**: 仅 `/api/auth/**` 和 `/uploads/**` 公开，其余全部需认证
- **数据隔离**: 服务层通过 `userId` 校验数据所有权
- **CORS**: 全局允许所有源（开发模式）

### JWT Token 结构
- 签名算法: HMAC-SHA256（HS256）
- Payload: `{sub: username, userId: Long, iat, exp}`
- 请求头: `Authorization: Bearer <token>`
- 过期返回: HTTP 401 `{"code":401,"msg":"Token无效或已过期，请重新登录"}`

## AI 分析说明

双模式设计：
- **有 DeepSeek Key**: 调用 `https://api.deepseek.com/chat/completions`，模型 `deepseek-chat`
- **无 DeepSeek Key**: 使用内置 Mock 数据（鸡胸肉、米饭、苹果、宫保鸡丁等）

`DeepSeek-R1` 模型映射到 `deepseek-reasoner`，`DeepSeek-V3` 映射到 `deepseek-chat`。
