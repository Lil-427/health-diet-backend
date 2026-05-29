package com.health.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户个人信息更新请求 DTO
 */
@Data
@ApiModel("个人信息更新请求")
public class UserProfileUpdateDTO {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty(value = "年龄", example = "25")
    private Integer age;

    @DecimalMin(value = "50", message = "身高不能低于 50cm")
    @DecimalMax(value = "250", message = "身高不能超过 250cm")
    @ApiModelProperty(value = "身高（cm）", example = "175.0")
    private Double height;

    @DecimalMin(value = "20", message = "体重不能低于 20kg")
    @DecimalMax(value = "300", message = "体重不能超过 300kg")
    @ApiModelProperty(value = "体重（kg）", example = "70.5")
    private Double weight;

    @ApiModelProperty(value = "健康目标", example = "减肥")
    private String goal;

    @ApiModelProperty(value = "性别: 0 未知 / 1 男 / 2 女", example = "1")
    private Integer gender;
}
