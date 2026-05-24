package com.poptrade.controller;

import com.poptrade.common.page.PageResult;
import com.poptrade.common.result.Result;
import com.poptrade.dto.ProductQueryDTO;
import com.poptrade.dto.ProductSaveDTO;
import com.poptrade.service.ProductService;
import com.poptrade.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理接口，仅管理员可访问。
 */
@Tag(name = "商品管理", description = "商品 CRUD + 上下架")
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询商品")
    @GetMapping("/page")
    public Result<PageResult<ProductVO>> page(@Valid ProductQueryDTO query) {
        return Result.success(productService.page(query));
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody ProductSaveDTO dto) {
        productService.save(dto);
        return Result.success();
    }

    @Operation(summary = "修改商品")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ProductSaveDTO dto) {
        productService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除商品")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        productService.deleteBatch(ids);
        return Result.success();
    }

    @Operation(summary = "上架/下架商品")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Parameter(description = "0=下架, 1=上架") @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }
}
