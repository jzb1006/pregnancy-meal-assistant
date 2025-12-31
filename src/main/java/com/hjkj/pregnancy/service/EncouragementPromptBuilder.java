package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.enums.MoodType;
import com.hjkj.pregnancy.enums.PregnancyStage;
import org.springframework.stereotype.Component;

/**
 * 每日鼓励语录 AI 提示词构建器
 * <p>
 * 根据孕周、心情、孕期阶段生成标准化的 AI 提示词。
 * 提示词设计遵循角色扮演、上下文设定、任务说明、规则约束的结构。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Component
public class EncouragementPromptBuilder {

    /**
     * 构建 AI 提示词
     *
     * @param week  孕周
     * @param mood  心情类型
     * @param stage 孕期阶段
     * @return 完整的 AI 提示词
     */
    public String buildPrompt(int week, MoodType mood, PregnancyStage stage) {
        String moodLabel = mood.getLabel();
        String stageGuidance = getStageGuidance(stage);
        String moodGuidance = getMoodGuidance(mood);

        return String.format("""
            # Role
            你现在是用户肚子里 **%d周大** 的宝宝。

            # User Context
            - 妈妈当前的孕周：**第 %d 周**（%s）
            - 妈妈今天的心情：**%s**

            # Task
            请生成以下内容：
            1. 给妈妈的暖心鼓励短句（**严格≤50个中文字符**，包含emoji）
            2. 宝宝当前状态描述（格式：像个XX，例如"像个柠檬"、"像个南瓜"）

            # Rules
            1. **第一人称：** 必须用"我"称呼宝宝，用"妈妈"称呼用户
            2. **结合孕周：** %s
            3. **针对心情：** %s
            4. **Emoji：** 鼓励短句结尾必须带上 1-2 个可爱的 Emoji
            5. **安全护栏：** 不要提供医疗建议、不要建议用药、异常不适建议就医
            6. **输出约束：**
               - 只输出一个 JSON 对象，不要代码块、不要多余解释
               - encouragement 严格≤50个中文字符
               - babySize 格式必须是"像个XX"，不要加标点或句子

            # Output Format (JSON)
            {
              "encouragement": "妈妈，我知道你最近背很痛，那是因为我长大了呀...",
              "babySize": "像个南瓜"
            }
            """, week, week, stage.getLabel(), moodLabel, stageGuidance, moodGuidance);
    }

    /**
     * 获取孕期阶段指导语
     */
    private String getStageGuidance(PregnancyStage stage) {
        return switch (stage) {
            case EARLY -> "我在努力扎根，妈妈要多休息哦";
            case MIDDLE -> "我正在快速成长，妈妈要保持营养均衡";
            case LATE -> "很快就要见面了，妈妈再坚持一下";
            case OVERDUE -> "我准备好了，妈妈我们马上就能见面啦";
        };
    }

    /**
     * 获取心情指导语
     */
    private String getMoodGuidance(MoodType mood) {
        return switch (mood) {
            case TIRED, ANXIOUS -> "安慰她，告诉她辛苦是为了我，我很感激";
            case HAPPY, EXCITED -> "和她一起开心，期待我们见面的那一天";
            case CALM -> "保持平和心态，我也感到很安心";
        };
    }
}