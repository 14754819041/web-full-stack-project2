package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.CartAddRequest;
import com.mall.dto.CartDTO;
import com.mall.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Slf4j
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody CartAddRequest request, HttpServletRequest httpRequest) {
        log.info("收到添加购物车请求，原始请求体：{}", request);
        try {
            // 打印请求头信息
            log.info("Content-Type: {}", httpRequest.getHeader("Content-Type"));
            
            // 参数校验
            if (request == null) {
                log.error("请求参数为空");
                return Result.error("请求参数不能为空");
            }
            
            log.info("解析后的请求参数：userId={}, productId={}, quantity={}", 
                    request.getUserId(), request.getProductId(), request.getQuantity());

            cartService.add(request.getUserId(), request.getProductId(), request.getQuantity());
            log.info("添加购物车成功");
            return Result.success();
        } catch (Exception e) {
            log.error("添加购物车失败：{}", e.getMessage(), e);
            return Result.error("添加购物车失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<CartDTO>> list(@RequestParam Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error("无效的用户ID");
            }
            return Result.success(cartService.list(userId));
        } catch (Exception e) {
            log.error("获取购物车列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestParam Long userId,
                              @RequestParam Long productId,
                              @RequestParam Integer quantity) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error("无效的用户ID");
            }
            if (productId == null || productId <= 0) {
                return Result.error("无效的商品ID");
            }
            if (quantity == null || quantity <= 0) {
                return Result.error("商品数量必须大于0");
            }
            cartService.update(userId, productId, quantity);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新购物车失败", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long userId,
                              @RequestParam Long productId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error("无效的用户ID");
            }
            if (productId == null || productId <= 0) {
                return Result.error("无效的商品ID");
            }
            cartService.delete(userId, productId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除购物车商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/selected")
    public Result<Void> updateSelected(@RequestParam Long userId,
                                     @RequestParam Long productId,
                                     @RequestParam Boolean selected) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error("无效的用户ID");
            }
            if (productId == null || productId <= 0) {
                return Result.error("无效的商品ID");
            }
            cartService.updateSelected(userId, productId, selected);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新商品选中状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/selected/all")
    public Result<Void> updateAllSelected(@RequestParam Long userId,
                                        @RequestParam Boolean selected) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error("无效的用户ID");
            }
            cartService.updateAllSelected(userId, selected);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新全部商品选中状态失败", e);
            return Result.error(e.getMessage());
        }
    }
} 