package com.poptrade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poptrade.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Update("""
            UPDATE product
            SET stock = stock - #{productNum}
            WHERE id = #{productId}
              AND status = 1
              AND stock >= #{productNum}
            """)
    int deductStock(@Param("productId") Long productId, @Param("productNum") Integer productNum);

    @Update("""
            UPDATE product
            SET stock = stock + #{productNum}
            WHERE id = #{productId}
            """)
    int restoreStock(@Param("productId") Long productId, @Param("productNum") Integer productNum);
}
