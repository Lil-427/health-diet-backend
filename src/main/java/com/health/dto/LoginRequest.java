package com.health.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 */
@Data
@ApiModel("登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;
}
