package com.poptrade.dto;

import com.poptrade.common.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryQueryDTO extends PageRequest {

    private String categoryName;
}
