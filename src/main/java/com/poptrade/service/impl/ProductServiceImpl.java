package com.poptrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.common.page.PageResult;
import com.poptrade.dto.ProductQueryDTO;
import com.poptrade.dto.ProductSaveDTO;
import com.poptrade.entity.Category;
import com.poptrade.entity.Product;
import com.poptrade.mapper.CategoryMapper;
import com.poptrade.mapper.ProductMapper;
import com.poptrade.service.ProductService;
import com.poptrade.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<ProductVO> page(ProductQueryDTO query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(query.getProductName()),
                        Product::getProductName, query.getProductName())
                .eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId())
                .eq(query.getStatus() != null, Product::getStatus, query.getStatus())
                .orderByAsc(Product::getCategoryId)
                .orderByAsc(Product::getId);

        Page<Product> page = productMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return PageResult.from(page, this::toVO);
    }

    @Override
    public ProductVO getById(Long id) {
        Product product = requireExists(id);
        return toVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(ProductSaveDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        if (dto.getStatus() == null) {
            product.setStatus(Product.STATUS_ON_SHELF);
        }
        productMapper.insert(product);
        log.info("新增商品: {}", product.getProductName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProductSaveDTO dto) {
        requireExists(dto.getId());
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        productMapper.updateById(product);
        log.info("修改商品: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        requireExists(id);
        productMapper.deleteById(id);
        log.info("删除商品: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的商品");
        }
        productMapper.deleteBatchIds(ids);
        log.info("批量删除商品: ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Product product = requireExists(id);
        product.setStatus(status);
        productMapper.updateById(product);
        log.info("切换商品状态: id={}, status={}", id, status);
    }

    /** Entity → VO，补充分类名称 */
    private ProductVO toVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        return vo;
    }

    private Product requireExists(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }
}
