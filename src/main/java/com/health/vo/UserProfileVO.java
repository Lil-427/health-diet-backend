package com.health.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户个人信息响应 VO（不包含密码）
 */
@Data
@ApiModel("用户个人信息")
public class UserProfileVO {

    @ApiModelProperty("用户 ID")
    private Long id;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("身高（cm）")
    private Double height;

    @ApiModelProperty("体重（kg）")
    private Double weight;

    @ApiModelProperty("健康目标")
    private String goal;

    @ApiModelProperty("性别: 0 未知 / 1 男 / 2 女")
    private Integer gender;

    @ApiModelProperty("头像 URL")
    private String avatar;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
