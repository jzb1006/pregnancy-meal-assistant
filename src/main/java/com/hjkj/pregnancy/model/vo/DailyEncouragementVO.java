package com.hjkj.pregnancy.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 每日鼓励语录响应 VO
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日鼓励语录信息")
public class DailyEncouragementVO {

    @Schema(description = "鼓励语录文本", example = "妈妈，我知道你最近背很痛，那是因为我长大了呀。再坚持一下，还有10周我们就能见面啦！爱你哟 ❤️")
    private String encouragement;

    @Schema(description = "当前孕周", example = "30")
    private Integer week;

    @Schema(description = "生成时的心情", example = "疲惫")
    private String mood;

    @Schema(description = "宝宝状态描述", example = "像个南瓜")
    private String babySize;

    @Schema(description = "生成时间", example = "2025-12-31T10:30:00")
    private LocalDateTime generatedAt;

    @Schema(description = "是否为降级文案", example = "false")
    private Boolean isFallback;
}