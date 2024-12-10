package com.mall.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.dto.ProductDTO;
import com.mall.dto.ProductSearchDTO;
import com.mall.entity.Category;
import com.mall.entity.Product;
import com.mall.mapper.CategoryMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Page<ProductDTO> list(ProductSearchDTO searchDTO) {
        Page<Product> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        
        // 使用自定义查询方法
        Page<Product> productPage = productMapper.searchProducts(
            page, 
            searchDTO.getCategoryId(),
            searchDTO.getKeyword(),
            searchDTO.getOrderBy()
        );
        
        // 获取所有分类并创建映射
        Map<Long, String> categoryMap = categoryMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(
                    Category::getId,
                    Category::getName,
                    (existing, replacement) -> existing
                ));
        
        // 转换为DTO
        Page<ProductDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(productPage, dtoPage, "records");
        
        List<ProductDTO> dtoList = productPage.getRecords().stream().map(product -> {
            ProductDTO dto = new ProductDTO();
            BeanUtils.copyProperties(product, dto);
            dto.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), "未知分类"));
            return dto;
        }).collect(Collectors.toList());
        
        // 设置分页信息
        dtoPage.setRecords(dtoList);
        dtoPage.setTotal(productPage.getTotal());
        dtoPage.setSize(searchDTO.getPageSize());
        dtoPage.setCurrent(searchDTO.getPageNum());
        dtoPage.setPages((productPage.getTotal() + searchDTO.getPageSize() - 1) / searchDTO.getPageSize());
        
        return dtoPage;
    }

    @Override
    public ProductDTO detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        
        // 获取分类名称
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            dto.setCategoryName(category.getName());
        }
        
        return dto;
    }
} 