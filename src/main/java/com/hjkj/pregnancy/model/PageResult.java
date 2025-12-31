package com.hjkj.pregnancy.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装
 *
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "总记录数", example = "100")
    private long total;

    @Schema(description = "当前页码", example = "1")
    private int page;

    @Schema(description = "每页大小", example = "10")
    private int size;

    @Schema(description = "总页数", example = "10")
    private int totalPages;

    @Schema(description = "数据列表")
    private List<T> list;

    /**
     * 计算总页数
     */
    public static int calculateTotalPages(long total, int size) {
        if (size <= 0)
            return 0;
        return (int) Math.ceil((double) total / size);
    }
}
