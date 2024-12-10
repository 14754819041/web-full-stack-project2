package com.mall.dto;

import lombok.Data;

@Data
public class ProductSearchDTO {
    private String keyword;
    private Long categoryId;
    private String orderBy;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Double minPrice;
    private Double maxPrice;
    private Integer minSales;
    private Boolean inStock;  // 是否有库存
}
