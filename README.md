# 智能健康饮食系统 — 后端

基于 Spring Boot 的智能健康饮食管理后端 API 服务，提供用户认证、饮食记录、营养统计、AI 智能分析等功能。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| Java | 17 | 开发语言 |
| Spring Security | 5.7.11 | 认证鉴权 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 5.7 / 8.0 | 数据库 |
| JWT (jjwt) | 0.11.5 | 无状态 Token 认证 |
| Lombok | 1.18.30 | 代码简化 |
| Bean Validation | - | 参数校验 |
| Swagger 注解 | 1.6.6 | API 文档标记 |
| DeepSeek API | - | AI 营养分析（可配置开关） |

## 项目结构

```
src/main/java/com/health/
├── HealthApplication.java              # 启动类
├── common/
│   ├── BusinessException.java          # 业务异常（可携带错误码）
│   ├── PageResult.java                 # 分页结果封装
│   ├── Result.java                     # 统一响应封装
│   └── ResultCode.java                 # 响应状态码枚举
├── config/
│   ├── CorsConfig.java                 # 跨域配置（限制 localhost）
│   ├── FileUploadConfig.java           # 文件上传静态资源映射
│   ├── MybatisPlusConfig.java          # MyBatis-Plus 分页插件
│   └── SecurityConfig.java             # Spring Security + JWT 无状态配置
├── controller/
│   ├── AiController.java               # AI 智能分析（手动/饮食/历史/建议）
│   ├── AuthController.java             # 认证（注册/登录）
│   ├── FoodRecordController.java       # 饮食记录 CRUD
│   ├── StatsController.java            # 数据统计（趋势/占比）
│   └── UserController.java             # 用户信息（资料/密码/头像/注销）
├── dto/
│   ├── AiAnalyzeDietRequest.java       # AI 饮食分析请求
│   ├── AiAnalyzeManualRequest.java     # AI 手动分析请求
│   ├── FoodRecordDTO.java              # 饮食记录请求
│   ├── LoginRequest.java               # 登录请求
│   ├── RegisterRequest.java            # 注册请求
│   ├── UpdatePasswordRequest.java      # 修改密码请求
│   └── UserProfileUpdateDTO.java       # 个人信息更新请求
├── entity/
│   ├── AiAnalysisLog.java              # AI 分析日志实体
│   ├── FoodRecord.java                 # 饮食记录实体
│   └── User.java                       # 用户实体
├── handler/
│   ├── GlobalExceptionHandler.java     # 全局异常处理（不泄露内部信息）
│   └── JwtAuthenticationTokenFilter.java  # JWT 认证过滤器
├── mapper/
│   ├── AiAnalysisLogMapper.java        # AI 分析日志 Mapper
│   ├── FoodRecordMapper.java           # 饮食记录 Mapper（含自定义查询）
│   └── UserMapper.java                 # 用户 Mapper
├── service/
│   ├── AiNutritionService.java         # AI 营养分析服务接口
│   ├── FoodRecordService.java          # 饮食记录服务接口
│   ├── StatsService.java               # 统计服务接口
│   ├── UserService.java                # 用户服务接口
│   └── impl/
│       ├── AiNutritionServiceImpl.java     # AI 分析实现（DeepSeek API + Mock 回退）
│       ├── FoodRecordServiceImpl.java      # 饮食记录实现（含权限校验）
│       ├── StatsServiceImpl.java           # 统计实现（趋势/营养素占比）
│       └── UserServiceImpl.java            # 用户服务实现（头像/密码/注销）
├── utils/
│   ├── JwtUtils.java                   # JWT 生成/解析
│   └── NutritionCalculator.java        # 营养计算工具（汇总/Mifflin-St Jeor）
└── vo/
    ├── AiAnalyzeDietVO.java            # AI 饮食分析结果
    ├── AiAnalyzeManualVO.java          # AI 手动分析结果
    ├── CalorieTrendVO.java             # 热量趋势
    ├── FoodRecordListVO.java           # 饮食记录列表（含营养汇总）
    ├── LoginResponse.java              # 登录响应
    ├── NutrientRatioVO.java            # 营养素占比
    ├── NutritionSummaryVO.java         # 营养汇总
    └── UserProfileVO.java              # 用户信息（不含密码）
```

## 快速开始

### 前置要求

- JDK 17
- Maven 3.6+
- MySQL 5.7+

### 环境变量

```bash
# 必需
export DB_PASSWORD=<你的数据库密码>
export JWT_SECRET=<Base64 编码的 JWT 密钥>
export DEEPSEEK_API_KEY=<你的 DeepSeek API Key>
```

> 不设置 `DEEPSEEK_API_KEY` 时，AI 接口自动回退到内置 Mock 数据。

### 数据库初始化

执行 `src/main/resources/db/init.sql` 或手动建表：

```sql
CREATE DATABASE health_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 用户表
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    height DOUBLE NULL,
    weight DOUBLE NULL,
    goal VARCHAR(100) NULL,
    gender INT DEFAULT 0 COMMENT '0未知 1男 2女',
    avatar VARCHAR(500) NULL,
    age INT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0 COMMENT '逻辑删除'
);

-- 饮食记录表
CREATE TABLE food_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    calories DOUBLE NOT NULL,
    protein DOUBLE NOT NULL,
    carbs DOUBLE NOT NULL,
    fat DOUBLE NOT NULL,
    meal_type VARCHAR(20) NOT NULL COMMENT 'breakfast/lunch/dinner/snack',
    record_date DATE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- AI 分析日志表
CREATE TABLE ai_analysis_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    analysis_type VARCHAR(20) NULL COMMENT 'manual/diet',
    calories DOUBLE NULL,
    protein DOUBLE NULL,
    carbs DOUBLE NULL,
    fat DOUBLE NULL,
    advice TEXT NULL,
    details TEXT NULL COMMENT 'JSON 格式详情',
    image_url VARCHAR(500) NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 启动

```bash
cd health-diet-backend
mvn spring-boot:run
```

启动后访问 `http://localhost:8080`。

---

## API 接口文档

> 需认证的接口在 Header 中携带：`Authorization: Bearer <token>`

### 响应格式

```json
{
    "code": 200,   // 200 成功 / 400 参数错误 / 401 未认证 / 403 无权限 / 500 服务器错误
    "msg": "操作成功",
    "data": {}
}
```

### 认证管理 — `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录，返回 JWT Token |

**注册**
```
POST /api/auth/register
{"username": "zhangsan", "password": "Test1234"}
```
响应：`{"code":200, "msg":"注册成功"}`

**登录**
```
POST /api/auth/login
{"username": "zhangsan", "password": "Test1234"}
```
响应：`{"code":200, "data":{"token":"eyJ...", "userId":1, "username":"zhangsan"}}`

---

### 用户信息 — `/api/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/profile` | 获取个人信息 |
| PUT | `/api/user/profile` | 更新资料（身高/体重/目标/年龄/性别） |
| PUT | `/api/user/password` | 修改密码（需旧密码验证） |
| POST | `/api/user/avatar` | 上传头像（multipart，≤2MB） |
| POST | `/api/user/avatar/base64` | 上传头像（Base64，≤5MB） |
| DELETE | `/api/user/account` | 注销账号（物理删除，不可撤销） |

---

### 饮食记录 — `/api/food/record`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/food/record` | 新增记录 |
| GET | `/api/food/record/list?date=2026-06-01` | 按日期查询（含当日营养汇总） |
| PUT | `/api/food/record/{id}` | 修改记录（只能修改自己的） |
| DELETE | `/api/food/record/{id}` | 删除记录（只能删除自己的） |

**新增记录**
```
POST /api/food/record
{
    "foodName": "米饭",
    "calories": 200,
    "protein": 4.0,
    "carbs": 45.0,
    "fat": 0.5,
    "mealType": "lunch",
    "recordDate": "2026-06-01"
}
```

---

### AI 智能分析 — `/api/ai`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/analyze/manual` | 手动输入食物，AI 分析营养 |
| POST | `/api/ai/analyze/diet` | 分析某天全部饮食记录 |
| GET | `/api/ai/profile-tip` | 根据用户画像生成个性化饮食建议 |
| GET | `/api/ai/analyze/history?page=1&pageSize=10` | 分析历史（分页） |
| DELETE | `/api/ai/analyze/{id}` | 删除单条分析记录 |
| DELETE | `/api/ai/analyze/history` | 清空所有分析历史 |

**手动分析**
```
POST /api/ai/analyze/manual
{"foodName": "鸡胸肉 150g", "model": "DeepSeek-V3"}
```

AI 模式说明：
- 配置了 `DEEPSEEK_API_KEY` → 调用真实 DeepSeek API
- 未配置 → 使用内置 Mock 数据（鸡胸肉、米饭、苹果、宫保鸡丁盖饭）
- `model` 可选值：`DeepSeek-V3`（默认）、`DeepSeek-R1`

---

### 数据统计 — `/api/stats`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stats/calorie-trend?range=7` | 热量趋势（7/30/90 天） |
| GET | `/api/stats/nutrient-ratio?date=2026-06-01` | 某天营养素占比 |
| GET | `/api/stats/nutrient-ratio?range=7` | 最近 N 天营养素占比 |

**热量趋势响应**
```json
{
    "days": ["05-26", "05-27", ...],
    "values": [1800, 2100, ...],
    "target": 2200,      // 根据用户身高体重目标动态计算的 Mifflin-St Jeor 推荐值
    "avgCal": 1950,
    "trend": "平稳"      // 上升/下降/平稳
}
```

**营养素占比响应**
```json
{
    "protein": {"percent": 25, "calories": 300},
    "carbs":   {"percent": 50, "calories": 600},
    "fat":     {"percent": 25, "calories": 300},
    "totalCal": 1200,
    "totalProtein": 75.0,
    "totalCarbs": 150.0,
    "totalFat": 33.3
}
```

热量计算公式：蛋白质×4 + 碳水×4 + 脂肪×9

---

## 认证流程

```
客户端                    后端                      数据库
  │  POST /api/auth/login   │                         │
  │────────────────────────→│                         │
  │                         │  查询用户                │
  │                         │────────────────────────→│
  │                         │←────────────────────────│
  │                         │  BCrypt 校验密码 → 生成 JWT │
  │  { token, userId }      │                         │
  │←────────────────────────│                         │
  │                         │                         │
  │  GET /api/user/profile  │                         │
  │  Authorization: Bearer xxx                        │
  │────────────────────────→│                         │
  │                         │  JWT 过滤器解析 userId    │
  │                         │  查询用户信息            │
  │                         │────────────────────────→│
  │                         │←────────────────────────│
  │  用户信息（不含密码）     │                         │
  │←────────────────────────│                         │
```

## 安全机制

- **认证**: JWT Bearer Token（HS256，7 天有效期）
- **密码加密**: BCryptPasswordEncoder
- **公开接口**: 仅 `/api/auth/**` 和 `/uploads/**` 无需认证
- **数据隔离**: Service 层通过 userId 校验所有权，403 拦截越权操作
- **异常处理**: 全局异常处理器统一返回格式，不泄露内部错误详情
- **CORS**: 仅允许 `localhost` 来源
- **文件上传**: 限制大小和类型，Base64 上传额外校验解码后大小

## 配置说明

主要配置在 `application.yml`，关键项：

| 配置项 | 说明 |
|--------|------|
| `server.port` | 服务端口（8080） |
| `spring.datasource.password` | 数据库密码（从环境变量读取） |
| `jwt.secret` | JWT 签名密钥（Base64 编码，环境变量） |
| `jwt.expiration` | Token 过期时间（604800000ms = 7天） |
| `deepseek.api-key` | DeepSeek API Key（环境变量，可选） |
| `deepseek.model` | 默认模型（deepseek-chat） |
| `deepseek.max-tokens` | 每次请求最大 Token 数（2000） |
| `deepseek.temperature` | 生成温度（0.7） |
| `file.upload-path` | 文件上传目录（./uploads） |

## 开发状态

- [x] 用户注册 / 登录（JWT + BCrypt）
- [x] 用户信息管理（资料编辑、密码修改、头像上传）
- [x] 饮食记录 CRUD（日期筛选、营养汇总、权限校验）
- [x] AI 营养分析（DeepSeek API + Mock 回退、历史管理）
- [x] 数据统计（热量趋势 7/30/90 天、营养素占比）
- [x] 个性化 AI 饮食建议（基于用户画像）
- [x] 账号注销
- [x] 前端界面（Vue 3 + Element Plus）
