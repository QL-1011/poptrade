package com.poptrade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poptrade.common.result.Result;
import com.poptrade.entity.Product;
import com.poptrade.mapper.CategoryMapper;
import com.poptrade.mapper.ProductMapper;
import com.poptrade.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 顾客端商品浏览接口。
 */
@Tag(name = "顾客端-商品", description = "顾客浏览商品")
@RestController
@RequestMapping("/api/customer/product")
@RequiredArgsConstructor
public class CustomerProductController {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "商品列表（按分类）")
    @GetMapping("/list")
    public Result<List<ProductVO>> list(@RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, Product.STATUS_ON_SHELF)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .orderByAsc(Product::getCategoryId)
                .orderByAsc(Product::getId);

        List<Product> products = productMapper.selectList(wrapper);

        // 批量查分类名称
        var categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getCategoryName()));

        List<ProductVO> list = products.stream().map(p -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(p, vo);
            vo.setCategoryName(categoryMap.getOrDefault(p.getCategoryId(), ""));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(list);
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() != Product.STATUS_ON_SHELF) {
            return Result.success(null);
        }
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        var category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }
        return Result.success(vo);
    }
}
