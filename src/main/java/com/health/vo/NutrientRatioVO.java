package com.health.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 营养素热量占比 VO
 */
@Data
@ApiModel("营养素热量占比")
public class NutrientRatioVO {

    @Data
    @ApiModel("营养元素")
    public static class NutrientItem {

        @ApiModelProperty("热量占比（%）")
        private Integer percent;

        @ApiModelProperty("热量值（千卡）")
        private Integer calories;
    }

    @ApiModelProperty("蛋白质")
    private NutrientItem protein;

    @ApiModelProperty("碳水化合物")
    private NutrientItem carbs;

    @ApiModelProperty("脂肪")
    private NutrientItem fat;

    @ApiModelProperty("总热量（千卡）")
    private Integer totalCal;

    @ApiModelProperty("总蛋白质（克）")
    private Double totalProtein;

    @ApiModelProperty("总碳水化合物（克）")
    private Double totalCarbs;

    @ApiModelProperty("总脂肪（克）")
    private Double totalFat;
}
