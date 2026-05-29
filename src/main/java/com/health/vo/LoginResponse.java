package com.health.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 VO
 */
@Data
@AllArgsConstructor
@ApiModel("登录响应")
public class LoginResponse {

    @ApiModelProperty("JWT Token")
    private String token;

    @ApiModelProperty("用户 ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;
}
