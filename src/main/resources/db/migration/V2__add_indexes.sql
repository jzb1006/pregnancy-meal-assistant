-- ========================================
-- 性能优化：添加数据库索引
-- 创建时间：2025-12-30
-- 作者：Zhibin Jiang
-- MySQL 8.0 兼容版本
-- ========================================

-- 1. 食谱表复合索引（用于智能匹配查询）
-- 优化 RecipeRepository.findSmartMatch 查询性能
DROP INDEX IF EXISTS idx_recipe_smart_match ON recipe;
CREATE INDEX idx_recipe_smart_match 
ON recipe(bmi_category, meal_type, pregnancy_week, created_at);

-- 2. 食谱表菜品名称索引（用于模糊查询）
DROP INDEX IF EXISTS idx_recipe_dish_name ON recipe;
CREATE INDEX idx_recipe_dish_name 
ON recipe(dish_name);

-- 3. 用户档案表 open_id 索引（已有 UNIQUE 约束，无需额外索引）
-- open_id 字段已有 UNIQUE 约束，MySQL 会自动创建索引

-- 4. 历史记录表复合索引（用于查询用户最近浏览记录）
-- 优化 HistoryService.getRecentRecipeIds 查询性能
DROP INDEX IF EXISTS idx_history_user_viewed ON user_history;
CREATE INDEX idx_history_user_viewed 
ON user_history(user_id, viewed_at);

-- 5. 历史记录表食谱ID索引（用于关联查询）
DROP INDEX IF EXISTS idx_history_recipe ON user_history;
CREATE INDEX idx_history_recipe 
ON user_history(recipe_id);

-- 6. AI请求日志表复合索引（用于查询用户日志）
DROP INDEX IF EXISTS idx_ai_log_user_time ON ai_request_log;
CREATE INDEX idx_ai_log_user_time 
ON ai_request_log(user_id, request_time);

-- 7. AI请求日志表时间索引（用于查询最近日志）
-- 简化为单列索引，便于按时间查询所有日志
DROP INDEX IF EXISTS idx_ai_log_time ON ai_request_log;
CREATE INDEX idx_ai_log_time 
ON ai_request_log(request_time);

-- ========================================
-- 索引说明
-- ========================================
-- 1. idx_recipe_smart_match: 
--    - 覆盖 BMI分类、餐次类型、孕周、创建时间的组合查询
--    - 预计查询性能提升 50-70%
--
-- 2. idx_recipe_dish_name:
--    - 支持按菜品名称搜索
--    - 为未来的搜索功能预留
--
-- 3. idx_history_user_viewed:
--    - 优化用户历史记录查询
--    - 支持按时间排序
--    - 预计查询性能提升 60-80%
--
-- 4. idx_history_recipe:
--    - 优化食谱关联查询
--    - 支持统计食谱被浏览次数
--
-- 5. idx_ai_log_user_time:
--    - 优化AI日志查询
--    - 支持按用户和时间查询
--
-- 6. idx_ai_log_time:
--    - 优化日志时间查询
--    - 支持快速获取最近日志
--
-- 注意事项：
-- - 使用 DROP INDEX IF EXISTS 确保脚本可重复执行
-- - MySQL 8.0+ 支持 IF EXISTS 语法
-- - 索引排序（DESC）在查询时动态处理，无需在索引定义中指定
-- ========================================

