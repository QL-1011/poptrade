package com.poptrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类新增/修改参数。
 */
@Data
public class CategorySaveDTO {

    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @NotNull(message = "排序不能为空")
    private Integer sort;
}
