-- 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS pregnancy_meal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pregnancy_meal;

-- 1. 用户档案表
CREATE TABLE IF NOT EXISTS `user_profile` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `open_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '用户唯一标识',
  `last_menstrual_period` DATE NOT NULL COMMENT '末次月经日期',
  `height` INT NOT NULL COMMENT '身高(cm)',
  `current_weight` DECIMAL(5,2) NOT NULL COMMENT '当前体重(kg)',
  `birth_date` DATE NOT NULL COMMENT '出生日期',
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
  INDEX `idx_pregnancy_week` (`pregnancy_week`)
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

