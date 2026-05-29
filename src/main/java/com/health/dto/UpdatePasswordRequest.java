package com.health.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码请求 DTO
 */
@Data
@ApiModel("修改密码请求")
public class UpdatePasswordRequest {

    @NotBlank(message = "旧密码不能为空")
    @ApiModelProperty(value = "旧密码", required = true, example = "123456")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 100, message = "新密码长度不能少于8位")
    @ApiModelProperty(value = "新密码", required = true, example = "654321")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @ApiModelProperty(value = "确认新密码", required = true, example = "654321")
    private String confirmPassword;
}
