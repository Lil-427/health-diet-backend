package com.health.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 饮食分析结果 VO
 */
@Data
@ApiModel("AI 饮食分析结果")
public class AiAnalyzeDietVO {

    @ApiModelProperty("分析记录 ID")
    private Long id;

    @ApiModelProperty("分析日期")
    private LocalDate date;

    @ApiModelProperty("营养汇总")
    private NutritionSummaryVO nutritionSummary;

    @ApiModelProperty("综合评分（优秀/良好/一般/较差）")
    private String score;

    @ApiModelProperty("综合评价")
    private String overallEval;

    @ApiModelProperty("优点")
    private List<String> pros;

    @ApiModelProperty("改进建议")
    private List<String> suggestions;

    @ApiModelProperty("分析时间")
    private LocalDateTime createTime;
}
