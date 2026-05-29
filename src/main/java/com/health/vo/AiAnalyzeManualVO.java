package com.health.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 手动分析结果 VO
 */
@Data
@ApiModel("AI 手动分析结果")
public class AiAnalyzeManualVO {

    @ApiModelProperty("分析记录 ID")
    private Long id;

    @ApiModelProperty("食物名称")
    private String foodName;

    @ApiModelProperty("份量")
    private String weight;

    @ApiModelProperty("热量（千卡）")
    private Double calories;

    @ApiModelProperty("蛋白质（克）")
    private Double protein;

    @ApiModelProperty("碳水化合物（克）")
    private Double carbs;

    @ApiModelProperty("脂肪（克）")
    private Double fat;

    @ApiModelProperty("微量元素详情")
    private List<Map<String, String>> details;

    @ApiModelProperty("饮食建议")
    private String advice;

    @ApiModelProperty("图片地址")
    private String imageUrl;

    @ApiModelProperty("是否为食物")
    private Boolean isFood;

    @ApiModelProperty("非食物时的提示消息")
    private String message;

    @ApiModelProperty("分析时间")
    private LocalDateTime createTime;
}
