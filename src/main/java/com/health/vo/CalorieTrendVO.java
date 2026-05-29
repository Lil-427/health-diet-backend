package com.health.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 热量趋势 VO
 */
@Data
@ApiModel("热量趋势")
public class CalorieTrendVO {

    @ApiModelProperty("日期列表（MM-dd）")
    private List<String> days;

    @ApiModelProperty("每日总热量列表（千卡）")
    private List<Integer> values;

    @ApiModelProperty("目标热量（千卡）")
    private Integer target;

    @ApiModelProperty("平均每日热量（千卡）")
    private Integer avgCal;

    @ApiModelProperty("趋势描述（上升/下降/平稳）")
    private String trend;
}
