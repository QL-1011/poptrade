package com.poptrade.controller;

import com.poptrade.common.page.PageResult;
import com.poptrade.common.result.Result;
import com.poptrade.dto.CategoryQueryDTO;
import com.poptrade.dto.CategorySaveDTO;
import com.poptrade.entity.Category;
import com.poptrade.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理接口，仅管理员可访问。
 */
@Tag(name = "分类管理", description = "商品分类 CRUD")
@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分页查询分类")
    @GetMapping("/page")
    public Result<PageResult<Category>> page(@Valid CategoryQueryDTO query) {
        return Result.success(categoryService.page(query));
    }

    @Operation(summary = "全量列表（商品下拉用）")
    @GetMapping("/list")
    public Result<List<Category>> listAll() {
        return Result.success(categoryService.listAll());
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody CategorySaveDTO dto) {
        categoryService.save(dto);
        return Result.success();
    }

    @Operation(summary = "修改分类")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody CategorySaveDTO dto) {
        categoryService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除分类")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        categoryService.deleteBatch(ids);
        return Result.success();
    }
}
