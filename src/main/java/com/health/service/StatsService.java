package com.health.service;

import com.health.vo.CalorieTrendVO;
import com.health.vo.NutrientRatioVO;

import java.time.LocalDate;

/**
 * 统计服务接口
 */
public interface StatsService {

    /**
     * 获取热量趋势
     *
     * @param userId 用户 ID
     * @param range  天数范围（7/30/90）
     * @return 热量趋势数据
     */
    CalorieTrendVO getCalorieTrend(Long userId, int range);

    /**
     * 获取营养素热量占比
     *
     * @param userId 用户 ID
     * @param date   查询日期（不传默认为今天）
     * @return 营养素占比数据
     */
    NutrientRatioVO getNutrientRatio(Long userId, LocalDate date);
}
