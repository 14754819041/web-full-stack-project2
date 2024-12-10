package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    Page<Product> searchProducts(Page<Product> page, 
                               @Param("categoryId") Long categoryId,
                               @Param("keyword") String keyword,
                               @Param("orderBy") String orderBy);
} 