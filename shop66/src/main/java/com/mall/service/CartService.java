package com.mall.service;

import com.mall.dto.CartDTO;
import java.util.List;

public interface CartService {
    CartDTO add(Long userId, Long productId, Integer quantity);
    void update(Long userId, Long productId, Integer quantity);
    void delete(Long userId, Long productId);
    void updateSelected(Long userId, Long productId, Boolean selected);
    void updateAllSelected(Long userId, Boolean selected);
    List<CartDTO> list(Long userId);
} 