package com.health.controller;

import com.health.common.Result;
import com.health.service.StatsService;
import com.health.vo.CalorieTrendVO;
import com.health.vo.NutrientRatioVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

/**
 * 数据统计控制器
 * 提供热量趋势和营养占比等统计信息
 */
@RestController
@RequestMapping("/api/stats")
@Api(tags = "数据统计")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 热量趋势（支持 7/30/90 天范围）
     *
     * @param range 天数范围
     */
    @GetMapping("/calorie-trend")
    @ApiOperation("热量趋势")
    public Result<CalorieTrendVO> calorieTrend(HttpServletRequest request,
                                                @RequestParam(defaultValue = "7")
                                                @ApiParam(value = "天数范围", example = "7") int range) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        CalorieTrendVO vo = statsService.getCalorieTrend(userId, range);
        return Result.success(vo);
    }

    /**
     * 营养素热量占比（支持指定日期，不传默认为今天）
     *
     * @param date 查询日期
     */
    @GetMapping("/nutrient-ratio")
    @ApiOperation("营养素热量占比")
    public Result<NutrientRatioVO> nutrientRatio(HttpServletRequest request,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                  @ApiParam(value = "查询日期", example = "2026-05-25") LocalDate date) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        if (date == null) {
            date = LocalDate.now();
        }

        NutrientRatioVO vo = statsService.getNutrientRatio(userId, date);
        return Result.success(vo);
    }
}
