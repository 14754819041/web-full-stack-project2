package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.UserDTO;
import com.mall.entity.User;
import com.mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody User user) {
        try {
            return Result.success(userService.register(user));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<UserDTO> login(@RequestParam String username, @RequestParam String password) {
        try {
            return Result.success(userService.login(username, password));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 