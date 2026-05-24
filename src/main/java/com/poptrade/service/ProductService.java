package com.poptrade.service;

import com.poptrade.common.page.PageResult;
import com.poptrade.dto.ProductQueryDTO;
import com.poptrade.dto.ProductSaveDTO;
import com.poptrade.vo.ProductVO;

import java.util.List;

/**
 * 商品管理服务接口。
 */
public interface ProductService {

    /** 分页 + 多条件筛选 */
    PageResult<ProductVO> page(ProductQueryDTO query);

    /** 根据 ID 查询 */
    ProductVO getById(Long id);

    /** 新增 */
    void save(ProductSaveDTO dto);

    /** 修改 */
    void update(ProductSaveDTO dto);

    /** 删除 */
    void deleteById(Long id);

    /** 批量删除 */
    void deleteBatch(List<Long> ids);

    /** 上架/下架 */
    void updateStatus(Long id, Integer status);
}
