package com.health.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * AI 手动分析请求 DTO
 */
@Data
@ApiModel("AI 手动分析请求")
public class AiAnalyzeManualRequest {

    @NotBlank(message = "食物名称不能为空")
    @ApiModelProperty(value = "食物名称", required = true, example = "鸡胸肉")
    private String foodName;

    @ApiModelProperty(value = "模型名称（预留）", example = "deepseek-chat")
    private String model;
}
