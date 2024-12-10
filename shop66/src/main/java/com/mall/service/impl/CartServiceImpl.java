package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mall.dto.CartDTO;
import com.mall.entity.Cart;
import com.mall.entity.Product;
import com.mall.mapper.CartMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public CartDTO add(Long userId, Long productId, Integer quantity) {
        log.info("添加购物车开始：userId={}, productId={}, quantity={}", userId, productId, quantity);
        
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            log.error("商品不存在：productId={}", productId);
            throw new RuntimeException("商品不存在");
        }
        
        // 检查库存
        if (product.getStock() < quantity) {
            log.error("商品库存不足：productId={}, stock={}, quantity={}", 
                productId, product.getStock(), quantity);
            throw new RuntimeException("商品库存不足");
        }
        
        // 查询购物车中是否已存在
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("product_id", productId);
        Cart cart = cartMapper.selectOne(wrapper);
        
        if (cart != null) {
            // 更新数量
            cart.setQuantity(cart.getQuantity() + quantity);
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(cart);
            log.info("更新购物车数量：cartId={}, quantity={}", cart.getId(), cart.getQuantity());
        } else {
            // 新增购物车记录
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelected(true);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
            log.info("新增购物车记录：cartId={}", cart.getId());
        }

        return convertToDTO(cart, product);
    }

    @Override
    @Transactional
    public void update(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 检查库存
        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }

        // 更新购物车数量
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("product_id", productId);
        Cart cart = cartMapper.selectOne(queryWrapper);
        if (cart == null) {
            throw new RuntimeException("购物车商品不存在");
        }

        cart.setQuantity(quantity);
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long productId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("product_id", productId);
        cartMapper.delete(queryWrapper);
    }

    @Override
    @Transactional
    public void updateSelected(Long userId, Long productId, Boolean selected) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("product_id", productId);
        Cart cart = cartMapper.selectOne(queryWrapper);
        if (cart == null) {
            throw new RuntimeException("购物车商品不存在");
        }

        cart.setSelected(selected);
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);
    }

    @Override
    @Transactional
    public void updateAllSelected(Long userId, Boolean selected) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        
        Cart updateCart = new Cart();
        updateCart.setSelected(selected);
        updateCart.setUpdateTime(LocalDateTime.now());
        
        cartMapper.update(updateCart, queryWrapper);
    }

    @Override
    public List<CartDTO> list(Long userId) {
        List<CartDTO> cartDTOs = cartMapper.selectCartDTOList(userId);
        // 计算每个商品的总价
        cartDTOs.forEach(cart -> {
            cart.setTotalPrice(cart.getProductPrice().multiply(new BigDecimal(cart.getQuantity())));
        });
        return cartDTOs;
    }

    private CartDTO convertToDTO(Cart cart, Product product) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setProductId(cart.getProductId());
        dto.setProductName(product.getName());
        dto.setProductImage(product.getMainImage());
        dto.setProductPrice(product.getPrice());
        dto.setQuantity(cart.getQuantity());
        dto.setSelected(cart.getSelected());
        dto.setCreateTime(cart.getCreateTime());
        dto.setUpdateTime(cart.getUpdateTime());
        dto.setTotalPrice(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        return dto;
    }
} 