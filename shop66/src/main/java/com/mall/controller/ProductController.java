package com.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.Result;
import com.mall.dto.ProductDTO;
import com.mall.dto.ProductSearchDTO;
import com.mall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<Page<ProductDTO>> list(ProductSearchDTO searchDTO) {
        return Result.success(productService.list(searchDTO));
    }

    @GetMapping("/detail/{id}")
    public Result<ProductDTO> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }
} 