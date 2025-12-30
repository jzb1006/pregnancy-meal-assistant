package com.hjkj.pregnancy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户反馈动作枚举
 *
 * @author Zhibin Jiang
 */
@Getter
@RequiredArgsConstructor
public enum FeedbackAction {
    LIKE("LIKE", "喜欢"),
    DISLIKE("DISLIKE", "不喜欢"),
    BORED("BORED", "吃腻了");

    private final String code;
    private final String description;
}
