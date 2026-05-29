package com.poptrade.controller;

import com.poptrade.common.result.Result;
import com.poptrade.entity.Category;
import com.poptrade.mapper.CategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 顾客端分类接口（与 admin CategoryController 功能相同，但走 /api/customer/** 路径，不需要管理员角色）。
 */
@Tag(name = "顾客端-分类", description = "顾客获取分类列表")
@RestController
@RequestMapping("/api/customer/category")
@RequiredArgsConstructor
public class CustomerCategoryController {

    private final CategoryMapper categoryMapper;

    @Operation(summary = "全量分类列表")
    @GetMapping("/list")
    public Result<List<Category>> listAll() {
        return Result.success(categoryMapper.selectList(null));
    }
}
