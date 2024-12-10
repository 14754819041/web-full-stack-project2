package com.mall.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotEmpty(message = "购物车ID不能为空")
    private List<Long> cartIds;
} 