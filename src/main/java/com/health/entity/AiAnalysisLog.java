package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 分析日志实体类
 * 对应数据库 ai_analysis_log 表
 */
@Data
@TableName("ai_analysis_log")
public class AiAnalysisLog {

    /** 日志 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 食物名称 */
    private String foodName;

    /** 分析类型（manual / diet） */
    private String analysisType;

    /** 热量（千卡） */
    private Double calories;

    /** 蛋白质（克） */
    private Double protein;

    /** 碳水化合物（克） */
    private Double carbs;

    /** 脂肪（克） */
    private Double fat;

    /** 分析建议 / 综合评价 */
    private String advice;

    /** 分析详情（JSON 格式，存放微量元素等额外数据） */
    private String details;

    /** 图片地址 */
    private String imageUrl;

    /** 创建时间 */
    private LocalDateTime createTime;
}
