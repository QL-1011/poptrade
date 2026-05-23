package com.poptrade.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poptrade.common.page.PageResult;
import com.poptrade.dto.CategoryQueryDTO;
import com.poptrade.dto.CategorySaveDTO;
import com.poptrade.entity.Category;

import java.util.List;

/**
 * 商品分类服务接口。
 */
public interface CategoryService {

    /** 分页 + 名称搜索 */
    PageResult<Category> page(CategoryQueryDTO query);

    /** 全量列表（供商品模块下拉选择） */
    List<Category> listAll();

    /** 新增 */
    void save(CategorySaveDTO dto);

    /** 修改 */
    void update(CategorySaveDTO dto);

    /** 删除 */
    void deleteById(Long id);

    /** 批量删除 */
    void deleteBatch(List<Long> ids);
}
