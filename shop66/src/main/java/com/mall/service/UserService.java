package com.mall.service;

import com.mall.dto.UserDTO;
import com.mall.entity.User;

public interface UserService {
    UserDTO register(User user);
    UserDTO login(String username, String password);
} 