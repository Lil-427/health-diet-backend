package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 饮食记录实体类
 * 对应数据库 food_record 表
 */
@Data
@TableName("food_record")
public class FoodRecord {

    /** 记录 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 食物名称 */
    private String foodName;

    /** 热量（千卡） */
    private Double calories;

    /** 蛋白质（克） */
    private Double protein;

    /** 碳水化合物（克） */
    private Double carbs;

    /** 脂肪（克） */
    private Double fat;

    /** 餐次（breakfast / lunch / dinner / snack） */
    private String mealType;

    /** 记录日期 */
    private LocalDate recordDate;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
