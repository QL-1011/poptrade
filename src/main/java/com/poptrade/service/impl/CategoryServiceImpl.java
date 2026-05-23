package com.poptrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.common.page.PageResult;
import com.poptrade.dto.CategoryQueryDTO;
import com.poptrade.dto.CategorySaveDTO;
import com.poptrade.entity.Category;
import com.poptrade.mapper.CategoryMapper;
import com.poptrade.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<Category> page(CategoryQueryDTO query) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .like(StringUtils.hasText(query.getCategoryName()),
                        Category::getCategoryName, query.getCategoryName())
                .orderByAsc(Category::getSort);

        Page<Category> page = categoryMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return PageResult.from(page, c -> c);
    }

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(CategorySaveDTO dto) {
        checkNameUnique(dto.getCategoryName(), null);
        Category category = new Category();
        category.setCategoryName(dto.getCategoryName());
        category.setSort(dto.getSort());
        categoryMapper.insert(category);
        log.info("新增分类: {}", category.getCategoryName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CategorySaveDTO dto) {
        requireExists(dto.getId());
        checkNameUnique(dto.getCategoryName(), dto.getId());
        Category category = categoryMapper.selectById(dto.getId());
        category.setCategoryName(dto.getCategoryName());
        category.setSort(dto.getSort());
        categoryMapper.updateById(category);
        log.info("修改分类: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        requireExists(id);
        categoryMapper.deleteById(id);
        log.info("删除分类: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的分类");
        }
        categoryMapper.deleteBatchIds(ids);
        log.info("批量删除分类: ids={}", ids);
    }

    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, name);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类名称已被占用");
        }
    }

    private void requireExists(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException("分类不存在");
        }
    }
}
