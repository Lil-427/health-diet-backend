package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库 user 表
 */
@Data
@TableName("user")
public class User {

    /** 用户 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 年龄 */
    private Integer age;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 身高（cm） */
    private Double height;

    /** 体重（kg） */
    private Double weight;

    /** 健康目标 */
    private String goal;

    /** 性别（0 未知 / 1 男 / 2 女） */
    private Integer gender;

    /** 头像 URL */
    private String avatar;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
