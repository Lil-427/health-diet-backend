package com.health.vo;

import com.health.entity.FoodRecord;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 饮食记录列表 VO
 */
@Data
@ApiModel("饮食记录列表")
public class FoodRecordListVO {

    @ApiModelProperty("饮食记录列表")
    private List<FoodRecord> records;

    @ApiModelProperty("当日营养汇总")
    private NutritionSummaryVO nutritionSummary;
}
