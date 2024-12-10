package com.mall.controller;

import com.mall.common.Result;
import com.mall.entity.Category;
import com.mall.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @GetMapping("/tree")
    public Result<List<Category>> tree() {
        return Result.success(categoryService.listTree());
    }
} 