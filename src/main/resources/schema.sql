-- 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS pregnancy_meal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pregnancy_meal;

-- 1. 用户档案表
CREATE TABLE IF NOT EXISTS `user_profile` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `open_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '用户唯一标识（微信OpenID）',
  `last_menstrual_period` DATE COMMENT '末次月经日期（静默登录可为空，完善档案时必填）',
  `height` INT COMMENT '身高(cm)（静默登录可为空，完善档案时必填）',
  `current_weight` DECIMAL(5,2) COMMENT '当前体重(kg)（静默登录可为空，完善档案时必填）',
  `birth_date` DATE COMMENT '出生日期（静默登录可为空，完善档案时必填）',
  `cuisine_preference` VARCHAR(30) COMMENT '饮食偏好: CHINESE/WESTERN/JAPANESE_KOREAN/SOUTHEAST_ASIAN/VEGETARIAN/NO_PREFERENCE',
  `allergies` VARCHAR(255) COMMENT '过敏源',
  `dietary_restrictions` VARCHAR(255) COMMENT '忌口',
  `preferences` VARCHAR(255) COMMENT '饮食强偏好',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户档案表';

-- 2. 智能食谱表 (缓存池)
CREATE TABLE IF NOT EXISTS `recipe` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `tags` VARCHAR(255) COMMENT '标签: 孕早期,补铁,控糖',
  `bmi_category` VARCHAR(50) DEFAULT 'ALL' COMMENT '适用BMI策略',
  `meal_type` VARCHAR(20) COMMENT '餐次类型: BREAKFAST/LUNCH/DINNER',
  `content_json` JSON NOT NULL COMMENT 'AI生成的完整JSON内容',
  `pregnancy_week` INT COMMENT '适用孕周',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_tags` (`tags`),
  INDEX `idx_bmi_category` (`bmi_category`),
  INDEX `idx_meal_type` (`meal_type`),
  INDEX `idx_pregnancy_week` (`pregnancy_week`),
  INDEX `idx_dish_name` (`dish_name`(50)) COMMENT '菜单名称索引，支持模糊搜索优化',
  INDEX `idx_meal_bmi` (`meal_type`, `bmi_category`) COMMENT '餐次和BMI复合索引，优化组合查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能食谱表';

-- 3. 浏览历史表
CREATE TABLE IF NOT EXISTS `user_history` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `recipe_id` BIGINT NOT NULL COMMENT '食谱ID',
  `viewed_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_recipe_id` (`recipe_id`),
  INDEX `idx_viewed_at` (`viewed_at`),
  INDEX `idx_user_viewed` (`user_id`, `viewed_at`) COMMENT '用户和浏览时间复合索引，优化分页查询',
  FOREIGN KEY (`user_id`) REFERENCES `user_profile`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`recipe_id`) REFERENCES `recipe`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览历史表';

-- 4. AI请求日志表
CREATE TABLE IF NOT EXISTS `ai_request_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` VARCHAR(100) COMMENT '用户标识',
  `scenario` VARCHAR(50) COMMENT '业务场景',
  `meal_type` VARCHAR(20) COMMENT '餐次类型',
  `prompt_content` TEXT COMMENT 'Prompt内容',
  `prompt_length` INT COMMENT 'Prompt长度',
  `response_content` TEXT COMMENT 'AI响应内容',
  `response_length` INT COMMENT '响应长度',
  `duration` BIGINT COMMENT '耗时(毫秒)',
  `is_success` BOOLEAN DEFAULT TRUE COMMENT '是否成功',
  `error_message` TEXT COMMENT '错误信息',
  `token_usage` INT COMMENT 'Token使用量',
  `model_name` VARCHAR(50) DEFAULT 'qwen-plus' COMMENT '模型名称',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_scenario` (`scenario`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI请求日志表';

-- 5. 用户反馈表
CREATE TABLE IF NOT EXISTS `user_feedback` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `recipe_id` BIGINT NOT NULL COMMENT '食谱ID',
  `action` VARCHAR(20) NOT NULL COMMENT '反馈动作: LIKE, DISLIKE, BORED',
  `reason` VARCHAR(255) COMMENT '反馈原因',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_recipe` (`user_id`, `recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';

-- 6. 每日鼓励语录表
CREATE TABLE IF NOT EXISTS `daily_encouragement` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `open_id` VARCHAR(64) NOT NULL COMMENT '用户唯一标识（微信OpenID）',
  `mood` VARCHAR(16) NOT NULL COMMENT '当日心情: HAPPY/TIRED/ANXIOUS/EXCITED/CALM',
  `encouragement_text` VARCHAR(200) NOT NULL COMMENT '鼓励语录文本',
  `baby_size` VARCHAR(32) NOT NULL COMMENT '宝宝状态描述（如"像个柠檬"）',
  `week` INT NOT NULL COMMENT '孕周',
  `generated_at` DATETIME NOT NULL COMMENT '生成时间',
  `record_date` DATE NOT NULL COMMENT '记录日期（用于唯一索引和查询）',
  `is_fallback` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为降级文案',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `uk_open_id_date` UNIQUE (`open_id`, `record_date`),
  INDEX `idx_open_id` (`open_id`),
  INDEX `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日鼓励语录表';

-- 7. 每日食谱推荐表 (NEW)
CREATE TABLE IF NOT EXISTS `daily_recommendation` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `rec_date` DATE NOT NULL COMMENT '日期 (e.g. 2026-01-04)',
  `week_num` INT NOT NULL COMMENT '生成时的孕周',
  `dish_name` VARCHAR(100) NOT NULL,
  `content_json` JSON NOT NULL COMMENT 'AI生成的完整菜谱',
  `rejected_history` TEXT COMMENT '今日已拒绝列表(逗号分隔)，用于AI去重',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_date` (`user_id`, `rec_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日食谱推荐表';

-- 8. 体重记录表
CREATE TABLE IF NOT EXISTS `weight_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `open_id` VARCHAR(64) NOT NULL COMMENT '用户标识(微信OpenID)',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `weight` DECIMAL(5,2) NOT NULL COMMENT '体重(kg)',
  `pregnancy_week` INT COMMENT '孕周',
  `note` VARCHAR(500) COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_openid_date` (`open_id`, `record_date`),
  INDEX `idx_openid_created` (`open_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体重记录表';

-- 9. 产检标准模板表
CREATE TABLE IF NOT EXISTS `prenatal_check_template` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '项目编码（唯一标识）',
  `week_range_start` INT NOT NULL COMMENT '孕周范围-开始',
  `week_range_end` INT NOT NULL COMMENT '孕周范围-结束',
  `title` VARCHAR(100) NOT NULL COMMENT '产检名称',
  `short_desc` VARCHAR(200) COMMENT '简短描述',
  `details` TEXT COMMENT '详细说明',
  `tips` TEXT COMMENT '注意事项',
  `stage` VARCHAR(20) NOT NULL COMMENT '孕期阶段：EARLY/MID/LATE',
  `stage_title` VARCHAR(50) NOT NULL COMMENT '阶段标题',
  `stage_icon` VARCHAR(10) COMMENT '阶段图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_stage_order` (`stage`, `sort_order`),
  INDEX `idx_week_range` (`week_range_start`, `week_range_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产检标准模板表';

-- 10. 用户产检完成状态表
CREATE TABLE IF NOT EXISTS `user_prenatal_check` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `open_id` VARCHAR(64) NOT NULL COMMENT '用户标识(微信OpenID)',
  `template_code` VARCHAR(50) NOT NULL COMMENT '产检项目编码',
  `is_done` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `check_date` DATE COMMENT '实际检查日期',
  `note` VARCHAR(500) COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_openid_code` (`open_id`, `template_code`),
  INDEX `idx_openid_done` (`open_id`, `is_done`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户产检完成状态表';
