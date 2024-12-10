package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mall.entity.Category;
import com.mall.mapper.CategoryMapper;
import com.mall.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> list() {
        return categoryMapper.selectList(new QueryWrapper<Category>()
                .orderByAsc("sort"));
    }

    @Override
    public List<Category> listTree() {
        // 获取所有分类
        List<Category> allCategories = list();
        
        // 按父ID分组
        Map<Long, List<Category>> parentMap = allCategories.stream()
                .collect(Collectors.groupingBy(category -> 
                    category.getParentId() == null ? 0L : category.getParentId()));
        
        // 构建树形结构
        return buildTree(parentMap, 0L);
    }
    
    private List<Category> buildTree(Map<Long, List<Category>> parentMap, Long parentId) {
        List<Category> children = parentMap.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }
        
        for (Category child : children) {
            List<Category> grandChildren = buildTree(parentMap, child.getId());
            if (!grandChildren.isEmpty()) {
                // 这里需要在Category类中添加children字段
                child.setChildren(grandChildren);
            }
        }
        
        return children;
    }
} 