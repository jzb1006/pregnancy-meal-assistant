

# 🏗️ 后端架构与需求文档 (Backend Spec)

**项目名称：** 孕妇今天吃啥 (Pregnancy Meal Assistant)
**版本：** v1.0 (MVP Backend Core)
**开发者：** Zhibin Jiang

## 1. 技术栈选型 (Tech Stack)

为了确保开发效率和对 AI 的良好支持，我们锁定以下版本：

* **语言:** **Java 21** 。
* **核心框架:** **Spring Boot 3.5+** (必须是 3.x 版本才能完美支持 Spring AI)。
* **AI 框架:** **Spring AI Alibaba** 。
* **数据库:** **MySQL 8.0** (支持 JSON 字段类型，这对存储 AI 生成的复杂结构非常有用)。
* **ORM:** **Spring Data JPA** (推荐) 或 MyBatis-Plus。
* *理由：* JPA 在处理对象与数据库映射时更标准，且 Spring AI 的返回对象可以直接映射为 Entity，开发速度快。


* **工具库:**
* `Lombok`: 简化样板代码。

* `Knife4j` / `Swagger`: 自动生成 API 文档，方便你后期写前端时查阅。



---

## 2. 系统架构设计 (Architecture)

采用经典的分层架构，增加一层 **AI Agent Layer**。

```mermaid
graph TD
    Client[前端小程序/Postman] --> Controller[Web Layer (API)]
    Controller --> Service[Service Layer (业务逻辑)]
    
    subgraph Core Logic
    Service --> Calculator[状态计算器 (孕周/BMI)]
    Service --> HistoryFilter[历史去重过滤器]
    Service --> AI_Agent[AI Agent Layer (Spring AI)]
    end
    
    AI_Agent --> LLM[大模型 API (GPT/DeepSeek)]
    
    Service --> Repository[DAO Layer]
    Repository --> DB[(MySQL Database)]

```

### 核心模块划分

1. **User Context Engine:** 负责计算用户的 `孕周`、`孕期阶段`、`BMI 指数`、`饮食策略`。
2. **Recommendation Engine:** 负责 `查缓存` -> `去重` -> `调 AI` -> `存库` 的完整闭环。
3. **Data Layer:** 负责用户档案、食谱库、浏览记录的 CRUD。

---

## 3. 数据库设计回顾 (Schema)

后端开发第一步，请在本地 MySQL 执行以下脚本。

```sql
-- 1. 用户档案表
CREATE TABLE `user_profile` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `open_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '用户唯一标识',
  `last_menstrual_period` DATE NOT NULL COMMENT '末次月经',
  `height` INT NOT NULL COMMENT '身高cm',
  `current_weight` DECIMAL(5,2) NOT NULL COMMENT '当前体重kg',
  `birth_date` DATE NOT NULL COMMENT '出生日期',
  `cuisine_preference` VARCHAR(30) COMMENT '饮食偏好',
  `allergies` VARCHAR(255) COMMENT '过敏源',
  `dietary_restrictions` VARCHAR(255) COMMENT '忌口',
  `preferences` VARCHAR(255) COMMENT '饮食强偏好',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. 智能食谱表 (缓存池)
CREATE TABLE `recipe` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `dish_name` VARCHAR(100) NOT NULL,
  `tags` VARCHAR(255) COMMENT '标签: 孕早期,补铁,控糖',
  `bmi_category` VARCHAR(50) DEFAULT 'ALL' COMMENT '适用BMI策略',
  `content_json` JSON NOT NULL COMMENT 'AI生成的完整JSON',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. 浏览历史表
CREATE TABLE `user_history` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `recipe_id` BIGINT NOT NULL,
  `viewed_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 4. 用户反馈表
CREATE TABLE `user_feedback` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `recipe_id` BIGINT NOT NULL,
  `action` VARCHAR(20) NOT NULL COMMENT 'LIKE, DISLIKE, BORED',
  `reason` VARCHAR(255) COMMENT '反馈原因',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_recipe` (`user_id`, `recipe_id`)
);

```

---

## 4. 接口定义 (API Endpoints)

我们需要提供 4 个核心接口。建议使用 `RestController`，统一返回结构 `Result<T>`。

### 4.1 用户初始化/更新

* **URL:** `POST /api/v1/user/profile`
* **作用:** 用户首次登陆或更新体重时调用。
* **Request Body:**
```json
{
  "openId": "wx_123456",
  "lmp": "2025-10-01",     // 末次月经
  "height": 165,
  "weight": 58.5,          // 当前体重
  "birthDate": "1990-05-15"  // 出生日期（必填）
}

```


* **Response:**
```json
{
  "week": 12,
  "bmi": 21.5,
  "bmiDesc": "标准",
  "stage": "孕早期",
  "age": 34,               // 当前年龄
  "tips": "宝宝现在像个柠檬，你保持得很棒！"
}

```



### 4.2 获取今日状态 (首页数据)

* **URL:** `GET /api/v1/user/status?openId=wx_123456`
* **作用:** 打开 App 首页时，获取当前的孕周和今日的一句话建议。

### 4.3 智能推荐 (核心接口)

* **URL:** `GET /api/v1/meal/recommend`
* **Query Params:**
* `openId`: 用户标识
* `mealType`: `BREAKFAST` / `LUNCH` / `DINNER`


* **逻辑:** 后端执行 孕周计算 -> BMI策略 -> 历史去重 -> AI生成/读库。
* **Response:**
```json
{
  "id": 101, // 菜谱ID
  "dishName": "彩椒炒牛肉粒",
  "reason": "针对孕中期且BMI微胖的你，这道菜高蛋白低脂...",
  "tags": ["补铁", "控糖"],
  "safety": "GREEN",
  "cookTime": "10分钟",
  "ingredients": ["牛肉 200g", "彩椒 1个"],
  "steps": ["切粒", "腌制", "快炒"],
  "husbandTask": "准爸爸负责切彩椒，并负责洗碗。"
}

```

### 4.3.1 智能推荐 - 流式接口 (新增)

* **URL:** `GET /api/v1/meal/recommend/stream`
* **Query Params:**
  * `openId`: 用户标识
  * `mealType`: `BREAKFAST` / `LUNCH` / `DINNER`

* **响应类型:** `text/event-stream` (Server-Sent Events)
* **特点:** 实时推送AI生成过程，提供更好的用户体验
* **逻辑:** 
  1. 如果数据库有缓存，直接返回 `complete` 事件
  2. 如果需要AI生成，先发送 `start` 事件，然后流式推送 `chunk` 事件，最后发送 `complete` 事件

* **事件类型:**
  * `start`: 开始生成
  * `chunk`: AI生成的内容片段
  * `complete`: 生成完成，包含完整的食谱对象
  * `error`: 错误信息

* **示例响应流:**
```
event: start
data: 开始生成食谱...

event: chunk
data: {"dish_name":"番茄

event: chunk
data: 炒蛋","reason":"富含蛋白质...

event: complete
data: {"id":101,"dishName":"番茄炒蛋",...}
```

* **使用说明:** 详见 [STREAM_API_USAGE.md](STREAM_API_USAGE.md)
* **测试页面:** 打开 [test-stream.html](test-stream.html) 进行测试

---

## 5. AI拦截器功能 (AI Interceptor)

### 5.1 功能概述

AI拦截器用于拦截和记录用户发送给AI的提示语（Prompt）和AI的响应内容，提供完整的请求追踪、日志记录和性能监控功能。

### 5.2 核心功能

* **请求拦截**: 记录用户发送的完整Prompt、请求时间、用户信息
* **响应拦截**: 记录AI的完整响应内容、响应时间、耗时、Token使用量
* **错误拦截**: 记录所有AI请求错误，便于问题排查
* **日志持久化**: 自动保存到数据库，支持异步处理

### 5.3 日志查询API

#### 查询用户日志
```
GET /api/v1/ai-log/user/{userId}
```

#### 查询最近日志
```
GET /api/v1/ai-log/recent
```

#### 查询失败日志
```
GET /api/v1/ai-log/failed
```

#### 按时间范围查询
```
GET /api/v1/ai-log/range?startTime=xxx&endTime=xxx
```

#### 统计用户请求
```
GET /api/v1/ai-log/stats/user/{userId}
```

#### 性能统计
```
GET /api/v1/ai-log/stats/performance
```

### 5.4 使用示例

```bash
# 发起AI请求（自动拦截）
curl 'http://localhost:8080/api/v1/meal/recommend?openId=test_user&mealType=BREAKFAST'

# 查询日志
curl 'http://localhost:8080/api/v1/ai-log/user/test_user'

# 查询性能统计
curl 'http://localhost:8080/api/v1/ai-log/stats/performance'
```

### 5.5 日志输出示例

控制台会输出详细的拦截信息：

```
================================================================================
AI请求拦截 [2025-12-30 10:30:00]
================================================================================
用户标识: test_user
业务场景: meal_recommend
餐次类型: BREAKFAST
Prompt长度: 1234 字符
--------------------------------------------------------------------------------
完整Prompt:
你是一位专业的孕期营养师，请为孕妇推荐一道早餐菜谱。
...
================================================================================
```

### 5.6 详细文档

* **使用指南**: [AI_INTERCEPTOR_GUIDE.md](AI_INTERCEPTOR_GUIDE.md)
* **功能总结**: [AI_INTERCEPTOR_SUMMARY.md](AI_INTERCEPTOR_SUMMARY.md)
* **数据库脚本**: [migration_add_ai_log.sql](migration_add_ai_log.sql)

```



### 4.4 获取浏览历史

* **URL:** `GET /api/v1/meal/history?openId=wx_123456`

---

## 5. 项目工程结构 (Project Structure)

建议创建如下的 Package 结构：

```
com.hjkj.pregnancy
├── config              # 配置类 (SpringAI Config, Swagger Config)
├── controller          # 接口层 (MealController, UserController)
├── entity              # 数据库实体 (UserProfile, Recipe)
├── repository          # DAO层 (JpaRepository)
├── service             # 业务接口
│   ├── impl            # 业务实现
│   │   ├── UserServiceImpl.java      # 负责计算孕周、BMI
│   │   ├── HistoryServiceImpl.java   # 负责记录和查询历史
│   │   └── RecommendationService.java # 核心：负责编排 AI 和 数据库逻辑
├── model               # 数据模型
│   ├── dto             # 前端传参对象 (UserInitRequest)
│   ├── vo              # 返回前端对象 (MealVO)
│   └── ai              # Spring AI 的映射 Record (AiMealRecord)
└── utils               # 工具类 (DateUtil, Result)

```

---

## 6. 核心业务逻辑伪代码 (Core Logic Specs)

你需要重点关注 `RecommendationService.java` 的实现。

```java
// 伪代码流程
public MealVO recommend(String openId, String mealType) {
    // 1. 查人：获取用户档案
    UserProfile user = userRepo.findByOpenId(openId);
    
    // 2. 算命：计算当前状态
    int week = calculateWeek(user.getLmp());
    String bmiStrategy = calculateBmiStrategy(user.getHeight(), user.getCurrentWeight());
    
    // 3. 查史：获取最近看过的菜 ID
    List<Long> viewedIds = historyRepo.findRecentIds(user.getId());
    
    // 4. 决策：先查库，库里没有再调 AI
    // 尝试在 DB 中找一个符合 tags=孕阶段 AND bmi=策略 AND id NOT IN viewedIds 的菜
    Recipe recipe = recipeRepo.findSmartMatch(week, bmiStrategy, viewedIds);
    
    if (recipe == null) {
        // 5. 调 AI：构建 Prompt
        String prompt = buildPrompt(week, bmiStrategy, mealType);
        AiMealRecord aiOutput = chatClient.call(prompt); // 这是一个耗时操作(2-5秒)
        
        // 6. 入库：保存新生成的菜
        recipe = saveRecipeToDb(aiOutput, week, bmiStrategy);
    }
    
    // 7. 留痕：记录本次浏览
    historyRepo.save(new History(user.getId(), recipe.getId()));
    
    return convertToVo(recipe);
}

```
