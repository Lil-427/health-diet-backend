package com.health.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 注册请求 DTO
 */
@Data
@ApiModel("注册请求")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 10, message = "用户名不能超过10个字符")
    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度不能少于8位")
    @ApiModelProperty(value = "密码", required = true, example = "abc12345")
    private String password;
}
