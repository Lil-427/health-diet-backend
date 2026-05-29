package com.health.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 饮食记录请求 DTO
 */
@Data
@ApiModel("饮食记录请求")
public class FoodRecordDTO {

    @NotBlank(message = "食物名称不能为空")
    @ApiModelProperty(value = "食物名称", required = true, example = "米饭")
    private String foodName;

    @NotNull(message = "热量不能为空")
    @DecimalMin(value = "0", message = "热量不能为负")
    @ApiModelProperty(value = "热量（千卡）", required = true, example = "200")
    private Double calories;

    @NotNull(message = "蛋白质不能为空")
    @DecimalMin(value = "0", message = "蛋白质不能为负")
    @ApiModelProperty(value = "蛋白质（克）", required = true, example = "4.0")
    private Double protein;

    @NotNull(message = "碳水化合物不能为空")
    @DecimalMin(value = "0", message = "碳水化合物不能为负")
    @ApiModelProperty(value = "碳水化合物（克）", required = true, example = "45.0")
    private Double carbs;

    @NotNull(message = "脂肪不能为空")
    @DecimalMin(value = "0", message = "脂肪不能为负")
    @ApiModelProperty(value = "脂肪（克）", required = true, example = "0.5")
    private Double fat;

    @NotBlank(message = "餐次不能为空")
    @ApiModelProperty(value = "餐次（breakfast / lunch / dinner / snack）", required = true, example = "lunch")
    private String mealType;

    @NotNull(message = "记录日期不能为空")
    @ApiModelProperty(value = "记录日期", required = true, example = "2026-05-25")
    private LocalDate recordDate;
}
