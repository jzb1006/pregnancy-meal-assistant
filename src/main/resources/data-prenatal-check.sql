-- ==========================================
-- 产检时光轴模块 - 初始数据脚本
-- ==========================================

-- 初始化产检标准模板数据
INSERT INTO `prenatal_check_template` 
(`code`, `week_range_start`, `week_range_end`, `title`, `short_desc`, `details`, `tips`, 
 `stage`, `stage_title`, `stage_icon`, `sort_order`, `is_active`) 
VALUES
-- 孕早期 (1-13周)
('first-check', 6, 8, '首次产检', '确认宫内孕、胎心胎芽', 
 'B超检查确认是否为宫内孕，查看胎心胎芽是否正常发育。建立母子健康手册。', 
 '记得空腹验血哦。', 
 'EARLY', '孕早期 (1-13周)', '🌱', 1, 1),

('nt-check', 11, 13, 'NT检查', '早期唐氏筛查', 
 '通过B超测量胎儿颈后透明层厚度，评估染色体异常风险。', 
 '主要看宝宝配合程度，不需要空腹。', 
 'EARLY', '孕早期 (1-13周)', '🌱', 2, 1),

-- 孕中期 (14-27周)
('tang-screening', 15, 20, '唐氏筛查', '中期唐筛 / 无创DNA', 
 '抽取孕妇静脉血，检测胎儿患唐氏综合征的风险。', 
 '空腹抽血，建议上午进行。', 
 'MIDDLE', '孕中期 (14-27周)', '👶', 3, 1),

('anomaly-scan', 20, 24, '大排畸', '四维彩超', 
 '系统性筛查胎儿结构畸形，包括面部、四肢、内脏等。', 
 '可以吃点巧克力让宝宝活跃一点。', 
 'MIDDLE', '孕中期 (14-27周)', '👶', 4, 1),

('glucose-test', 24, 28, '糖耐量试验', '筛查妊娠糖尿病', 
 '口服75g葡萄糖，分别在空腹、1小时、2小时抽血检测血糖。', 
 '前一晚清淡饮食，检查期间禁食禁水。', 
 'MIDDLE', '孕中期 (14-27周)', '👶', 5, 1),

-- 孕晚期 (28-40周)
('small-anomaly', 28, 30, '小排畸', '晚期B超筛查', 
 '再次确认胎儿生长发育情况，补漏筛查。', 
 NULL, 
 'LATE', '孕晚期 (28-40周)', '🤱', 6, 1),

('fetal-position', 36, 37, '胎位监测', '评估分娩方式', 
 '检查胎位（头位/臀位），骨盆测量，确定生产方式。', 
 NULL, 
 'LATE', '孕晚期 (28-40周)', '🤱', 7, 1)
ON DUPLICATE KEY UPDATE 
  `week_range_start` = VALUES(`week_range_start`),
  `week_range_end` = VALUES(`week_range_end`),
  `title` = VALUES(`title`),
  `short_desc` = VALUES(`short_desc`),
  `details` = VALUES(`details`),
  `tips` = VALUES(`tips`),
  `stage` = VALUES(`stage`),
  `stage_title` = VALUES(`stage_title`),
  `stage_icon` = VALUES(`stage_icon`),
  `sort_order` = VALUES(`sort_order`),
  `is_active` = VALUES(`is_active`);

