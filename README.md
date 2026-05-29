# 智能健康饮食系统

基于 **Vue + Spring Boot** 的智能健康饮食系统后端 API 服务。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| Java | 17 | 开发语言 |
| Spring Security | 5.7.11 | 用户认证鉴权 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0.33 | 数据库 |
| JWT (jjwt) | 0.11.5 | 无状态 Token 认证 |
| Caffeine | 2.9.3 | 本地缓存 |
| Lombok | 1.18.30 | 代码简化 |
| Bean Validation | - | 参数校验 |
| Swagger 注解 | 1.6.6 | API 标记 |

## 项目结构

```
src/main/java/com/health/
├── HealthApplication.java              # 启动类
├── common/
│   ├── PageResult.java                 # 分页结果封装
│   ├── Result.java                     # 统一响应封装
│   └── ResultCode.java                 # 响应状态码枚举
├── config/
│   ├── CorsConfig.java                 # 跨域配置
│   ├── FileUploadConfig.java           # 文件上传静态资源映射
│   ├── MybatisPlusConfig.java          # MyBatis-Plus 分页配置
│   └── SecurityConfig.java             # Spring Security JWT 配置
├── controller/
│   ├── AiController.java               # AI 智能分析接口
│   ├── AuthController.java             # 认证接口（注册/登录）
│   ├── FoodRecordController.java       # 饮食记录 CRUD 接口
│   ├── StatsController.java            # 数据统计接口
│   └── UserController.java             # 用户信息接口
├── dto/
│   ├── AiAnalyzeDietRequest.java       # AI 饮食分析请求 DTO
│   ├── AiAnalyzeManualRequest.java     # AI 手动分析请求 DTO
│   ├── AiAnalyzeRequest.java           # AI 分析请求 DTO（旧）
│   ├── FoodRecordDTO.java              # 饮食记录请求 DTO
│   ├── LoginRequest.java               # 登录请求 DTO
│   ├── RegisterRequest.java            # 注册请求 DTO
│   ├── UpdatePasswordRequest.java      # 修改密码请求 DTO
│   └── UserProfileUpdateDTO.java       # 个人信息更新 DTO
├── entity/
│   ├── AiAnalysisLog.java              # AI 分析日志实体
│   ├── FoodRecord.java                 # 饮食记录实体
│   └── User.java                       # 用户实体
├── handler/
│   ├── GlobalExceptionHandler.java     # 全局异常处理
│   └── JwtAuthenticationTokenFilter.java  # JWT 认证过滤器
├── mapper/
│   ├── AiAnalysisLogMapper.java        # AI 分析日志 Mapper
│   ├── FoodRecordMapper.java           # 饮食记录 Mapper
│   └── UserMapper.java                 # 用户 Mapper
├── service/
│   ├── AiNutritionService.java         # AI 营养分析服务接口
│   ├── FoodRecordService.java          # 饮食记录服务接口
│   ├── StatsService.java               # 统计服务接口
│   ├── UserService.java                # 用户服务接口
│   └── impl/
│       ├── AiNutritionServiceImpl.java     # AI 营养分析实现（Mock）
│       ├── FoodRecordServiceImpl.java      # 饮食记录服务实现
│       ├── StatsServiceImpl.java           # 统计服务实现
│       └── UserServiceImpl.java            # 用户服务实现
├── utils/
│   └── JwtUtils.java                   # JWT 工具类
└── vo/
    ├── AiAnalyzeDietVO.java            # AI 饮食分析结果 VO
    ├── AiAnalyzeManualVO.java          # AI 手动分析结果 VO
    ├── AiAnalyzeResponse.java          # AI 分析结果 VO（旧）
    ├── CalorieTrendVO.java             # 热量趋势 VO
    ├── FoodRecordListVO.java           # 饮食记录列表 VO（含营养汇总）
    ├── LoginResponse.java              # 登录响应 VO
    ├── NutrientRatioVO.java            # 营养热量占比 VO
    ├── NutritionSummaryVO.java         # 营养汇总 VO
    └── UserProfileVO.java              # 用户个人信息 VO（无密码）
```

## 快速开始

### 前置要求

- JDK 17
- Maven 3.6+
- MySQL 8.0+

### 配置数据库

1. 创建数据库 `health_db`：

```sql
CREATE DATABASE health_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 创建数据表：

```sql
-- 用户表
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    height DOUBLE NULL,
    weight DOUBLE NULL,
    goal VARCHAR(100) NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
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
    analysis_type VARCHAR(20) NULL COMMENT 'manual / diet',
    calories DOUBLE NULL,
    protein DOUBLE NULL,
    carbs DOUBLE NULL,
    fat DOUBLE NULL,
    advice TEXT NULL COMMENT '分析建议或评价JSON',
    details TEXT NULL COMMENT '分析详情JSON',
    image_url VARCHAR(500) NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

3. 修改 `src/main/resources/application.yml` 中的数据库密码。

### 启动项目

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run
```

启动后访问 `http://localhost:8080`。

## API 接口文档

> 所有需要认证的接口需在 Header 中携带 Token：`Authorization: Bearer <token>`

### 认证管理 — `/api/auth`

#### 用户注册

```
POST /api/auth/register
Content-Type: application/json

{
    "username": "zhangsan",
    "password": "123456"
}
```

响应：
```json
{
    "code": 200,
    "msg": "注册成功",
    "data": null
}
```

#### 用户登录

```
POST /api/auth/login
Content-Type: application/json

{
    "username": "zhangsan",
    "password": "123456"
}
```

响应：
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userId": 1,
        "username": "zhangsan"
    }
}
```

---

### 用户信息 — `/api/user`

#### 获取个人信息

```
GET /api/user/profile
Authorization: Bearer <token>
```

响应：
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "username": "zhangsan",
        "height": 175.0,
        "weight": 70.0,
        "goal": "减肥",
        "createTime": "2026-05-25 10:00:00"
    }
}
```

> 密码字段不会返回

#### 更新个人信息

```
PUT /api/user/profile
Content-Type: application/json
Authorization: Bearer <token>

{
    "height": 175.0,
    "weight": 70.5,
    "goal": "增肌"
}
```

#### 修改密码

```
PUT /api/user/password
Content-Type: application/json
Authorization: Bearer <token>

{
    "oldPassword": "123456",
    "newPassword": "654321",
    "confirmPassword": "654321"
}
```

> 校验旧密码正确性，新密码用 BCrypt 加密存储

#### 上传头像

```
POST /api/user/avatar
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: [头像图片]
```

响应：
```json
{
    "code": 200,
    "msg": "上传成功",
    "data": "/uploads/avatars/xxx.jpg"
}
```

> 支持 jpg/jpeg/png 格式，文件大小不超过 2MB

---

### 饮食记录 — `/api/food/record`

#### 新增记录

```
POST /api/food/record
Content-Type: application/json
Authorization: Bearer <token>

{
    "foodName": "米饭",
    "calories": 200,
    "protein": 4.0,
    "carbs": 45.0,
    "fat": 0.5,
    "mealType": "lunch",
    "recordDate": "2026-05-25"
}
```

#### 查询某天记录（含营养汇总）

```
GET /api/food/record/list?date=2026-05-25
Authorization: Bearer <token>
```

响应：
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "userId": 1,
                "foodName": "米饭",
                "calories": 200,
                "protein": 4.0,
                "carbs": 45.0,
                "fat": 0.5,
                "mealType": "lunch",
                "recordDate": "2026-05-25",
                "createTime": "2026-05-25T12:00:00",
                "updateTime": "2026-05-25T12:00:00"
            }
        ],
        "nutritionSummary": {
            "totalCal": 200.0,
            "totalProtein": 4.0,
            "totalCarbs": 45.0,
            "totalFat": 0.5,
            "targetCal": 0.0,
            "progress": 0.0
        }
    }
}
```

#### 更新记录

```
PUT /api/food/record/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
    "foodName": "米饭",
    "calories": 250,
    ...
}
```

> 只能更新自己的记录

#### 删除记录

```
DELETE /api/food/record/{id}
Authorization: Bearer <token>
```

---

### AI 智能分析 — `/api/ai`

#### 手动分析食物营养

```
POST /api/ai/analyze/manual
Content-Type: application/json
Authorization: Bearer <token>

{
    "foodName": "鸡胸肉",
    "model": "deepseek-chat"
}
```

响应：
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "foodName": "鸡胸肉",
        "weight": "100g",
        "calories": 165.0,
        "protein": 31.0,
        "carbs": 0.0,
        "fat": 3.6,
        "details": [
            {"name": "胆固醇", "val": "85", "unit": "mg"},
            {"name": "钠", "val": "74", "unit": "mg"},
            {"name": "钾", "val": "256", "unit": "mg"}
        ],
        "advice": "鸡胸肉是优质高蛋白低脂肪食物，适合减脂期食用。",
        "imageUrl": null,
        "createTime": "2026-05-25T12:00:00"
    }
}
```

内置 Mock 数据：

| 食物 | 热量 | 蛋白质 | 碳水 | 脂肪 | 建议 |
|------|------|--------|------|------|------|
| 鸡胸肉 | 165 | 31g | 0g | 3.6g | 高蛋白低脂肪，适合减脂 |
| 米饭 | 200 | 4g | 45g | 0.5g | 日常主食，补充碳水 |
| 苹果 | 95 | 0.5g | 25g | 0.3g | 富含维生素，适合加餐 |
| 宫保鸡丁盖饭 | 650 | 25g | 80g | 22g | 热量较高，控制摄入 |
| 其他 | 300 | 10g | 40g | 10g | 仅供参考 |

#### 饮食记录分析

```
POST /api/ai/analyze/diet
Content-Type: application/json
Authorization: Bearer <token>

{
    "date": "2026-05-25",
    "model": "deepseek-chat"
}
```

响应：
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 2,
        "date": "2026-05-25",
        "nutritionSummary": {
            "totalCal": 1200.0,
            "totalProtein": 55.0,
            "totalCarbs": 130.0,
            "totalFat": 30.0,
            "targetCal": 0.0,
            "progress": 0.0
        },
        "score": "良好",
        "overallEval": "今日饮食搭配较为均衡，蛋白质摄入充足。",
        "pros": ["蛋白质来源优质", "脂肪摄入合理"],
        "suggestions": ["碳水化合物摄入稍低", "水果摄入不足"],
        "createTime": "2026-05-25T12:00:00"
    }
}
```

> 分析日志保存在 `ai_analysis_log` 表中，`analysis_type` 区分 manual / diet

#### 查询分析历史（分页）

```
GET /api/ai/analyze/history?page=1&pageSize=10
Authorization: Bearer <token>
```

响应：
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [...],
        "total": 5,
        "page": 1,
        "pageSize": 10,
        "pages": 1
    }
}
```

#### 删除单条分析记录

```
DELETE /api/ai/analyze/{id}
Authorization: Bearer <token>
```

#### 清空分析历史

```
DELETE /api/ai/analyze/history
Authorization: Bearer <token>
```

---

### 数据统计 — `/api/stats`

#### 热量趋势（支持 7/30/90 天）

```
GET /api/stats/calorie-trend?range=7
Authorization: Bearer <token>
```

响应：
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "days": ["05-19", "05-20", "05-21", "05-22", "05-23", "05-24", "05-25"],
        "values": [0, 0, 0, 650, 800, 200, 450],
        "target": 2000,
        "avgCal": 300,
        "trend": "平稳"
    }
}
```

| 字段 | 说明 |
|------|------|
| target | 默认目标热量（2000千卡） |
| avgCal | 选定范围内的日均热量 |
| trend | 趋势描述：上升 / 下降 / 平稳 |

#### 营养素热量占比（支持指定日期）

```
GET /api/stats/nutrient-ratio?date=2026-05-25
Authorization: Bearer <token>
```

> `date` 参数可选，不传默认为今天

响应：
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "protein": {
            "percent": 20,
            "calories": 240
        },
        "carbs": {
            "percent": 55,
            "calories": 660
        },
        "fat": {
            "percent": 25,
            "calories": 300
        },
        "totalCal": 1200,
        "totalProtein": 60.0,
        "totalCarbs": 165.0,
        "totalFat": 33.3
    }
}
```

热量计算公式：

```
蛋白质热量 = 蛋白质克数 × 4
碳水热量   = 碳水克数 × 4
脂肪热量   = 脂肪克数 × 9
占比       = 该类热量 ÷ 总热量 × 100%
```

---

## 认证流程

```
┌─────────┐         ┌──────────────┐         ┌─────────┐
│  客户端  │         │  后端服务    │         │ 数据库  │
└────┬────┘         └──────┬───────┘         └────┬────┘
     │  POST /api/auth/login │                     │
     │─────────────────────→│                     │
     │                      │  查询用户            │
     │                      │────────────────────→│
     │                      │←────────────────────│
     │                      │  校验密码 → 生成JWT  │
     │←─────────────────────│                     │
     │  返回 { token }      │                     │
     │                      │                     │
     │  GET /api/user/profile│                    │
     │  Authorization: Bearer xxx                  │
     │─────────────────────→│                     │
     │                      │  解析 Token → userId │
     │                      │  查询用户信息        │
     │                      │────────────────────→│
     │                      │←────────────────────│
     │←─────────────────────│                     │
     │  返回用户信息         │                     │
```

## 请求/响应格式

### 统一响应格式

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {}
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未认证 / Token 无效 |
| 500 | 服务器内部错误 |

### 分页响应格式

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [...],
        "total": 100,
        "page": 1,
        "pageSize": 10,
        "pages": 10
    }
}
```

### 统一错误处理

所有异常由 `GlobalExceptionHandler` 统一处理：

- 参数校验失败 → `400 + 具体错误信息`
- 用户名不存在 / 密码错误 → `401`
- 运行时异常 → `500 + 错误详情`

## 配置说明

主要配置在 `src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/health_db
    username: root
    password: 123456

jwt:
  secret: YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcA==
  expiration: 604800000  # 7天

file:
  upload-path: ./uploads
  avatar-path: ./uploads/avatars
```

## 数据表 SQL

```sql
-- 用户表
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    height DOUBLE NULL,
    weight DOUBLE NULL,
    goal VARCHAR(100) NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
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
    analysis_type VARCHAR(20) NULL COMMENT 'manual / diet',
    calories DOUBLE NULL,
    protein DOUBLE NULL,
    carbs DOUBLE NULL,
    fat DOUBLE NULL,
    advice TEXT NULL COMMENT '分析建议或评价JSON',
    details TEXT NULL COMMENT '分析详情JSON',
    image_url VARCHAR(500) NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 开发计划

- [x] 用户注册 / 登录
- [x] JWT 认证
- [x] 用户信息管理（含修改密码、上传头像）
- [x] 饮食记录 CRUD（含营养汇总）
- [x] AI 营养分析（Mock 手动分析 + 饮食评价 + 历史管理）
- [x] 数据统计（热量趋势 / 营养占比 / 多范围支持）
- [ ] Vue 前端界面
- [ ] 真实 AI API 对接（DeepSeek）
- [ ] 饮食推荐功能
- [ ] 健康报告生成
