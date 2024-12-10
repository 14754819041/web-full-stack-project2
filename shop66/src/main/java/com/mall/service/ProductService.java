package com.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.dto.ProductDTO;
import com.mall.dto.ProductSearchDTO;

public interface ProductService {
    Page<ProductDTO> list(ProductSearchDTO searchDTO);
    ProductDTO detail(Long id);
} 