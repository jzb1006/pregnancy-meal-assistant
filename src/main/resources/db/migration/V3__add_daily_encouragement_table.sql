-- ========================================
-- 新增每日鼓励语录表
-- 创建时间：2025-12-31
-- 作者：Zhibin Jiang
-- MySQL 8.0 兼容版本
-- ========================================

-- 每日鼓励语录表
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

  -- 唯一约束：同一用户每天只能有一条记录
  CONSTRAINT `uk_open_id_date` UNIQUE (`open_id`, `record_date`),

  -- 索引优化
  INDEX `idx_open_id` (`open_id`),
  INDEX `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日鼓励语录表';

-- ========================================
-- 表设计说明
-- ========================================
-- 1. 唯一约束 uk_open_id_date:
--    - 确保同一用户每天只能有一条鼓励记录
--    - 支持幂等性查询（同一天多次请求返回相同内容）
--
-- 2. 字段说明:
--    - mood: 用户当日心情，影响AI生成的语录风格
--    - encouragement_text: 宝宝视角的鼓励语录（≤50字）
--    - baby_size: AI生成的宝宝状态描述
--    - week: 记录生成时的孕周，用于历史数据分析
--    - generated_at: 精确生成时间
--    - record_date: 业务日期（用于查询和唯一性控制）
--    - is_fallback: 标记是否为AI调用失败后的降级文案
--
-- 3. 索引策略:
--    - idx_open_id: 支持按用户查询历史记录
--    - idx_record_date: 支持按日期范围查询
--    - uk_open_id_date: 唯一约束自带索引，优化查询性能
--
-- 4. 业务逻辑:
--    - 每日首次请求：AI生成 → 存储到数据库 → 缓存
--    - 同日后续请求：缓存命中 → 直接返回（或从数据库查询）
--    - AI失败时：使用预定义降级文案，标记 is_fallback=true
-- ========================================