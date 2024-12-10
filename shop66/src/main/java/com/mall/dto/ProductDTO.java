package com.mall.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;  // 新增分类名称
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String mainImage;
    private String images;
    private Integer sales;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 