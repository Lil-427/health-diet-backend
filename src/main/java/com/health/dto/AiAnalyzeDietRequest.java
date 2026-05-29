package com.health.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * AI 饮食记录分析请求 DTO
 */
@Data
@ApiModel("AI 饮食记录分析请求")
public class AiAnalyzeDietRequest {

    @NotNull(message = "日期不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @ApiModelProperty(value = "分析日期", required = true, example = "2026-05-25")
    private LocalDate date;

    @ApiModelProperty(value = "模型名称（预留）", example = "deepseek-chat")
    private String model;
}
