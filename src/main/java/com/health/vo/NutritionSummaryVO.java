package com.health.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 营养汇总 VO
 */
@Data
@ApiModel("营养汇总")
public class NutritionSummaryVO {

    @ApiModelProperty("总热量（千卡）")
    private Double totalCal;

    @ApiModelProperty("总蛋白质（克）")
    private Double totalProtein;

    @ApiModelProperty("总碳水化合物（克）")
    private Double totalCarbs;

    @ApiModelProperty("总脂肪（克）")
    private Double totalFat;

    @ApiModelProperty("目标热量（千卡）")
    private Double targetCal;

    @ApiModelProperty("进度百分比")
    private Double progress;
}
