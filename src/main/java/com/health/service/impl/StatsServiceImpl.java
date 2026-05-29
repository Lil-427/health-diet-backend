package com.health.service.impl;

import com.health.entity.FoodRecord;
import com.health.mapper.FoodRecordMapper;
import com.health.service.StatsService;
import com.health.vo.CalorieTrendVO;
import com.health.vo.NutrientRatioVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 */
@Service
public class StatsServiceImpl implements StatsService {

    /** 蛋白质每克热量（千卡） */
    private static final int PROTEIN_CAL = 4;

    /** 碳水化合物每克热量（千卡） */
    private static final int CARBS_CAL = 4;

    /** 脂肪每克热量（千卡） */
    private static final int FAT_CAL = 9;

    private final FoodRecordMapper foodRecordMapper;

    public StatsServiceImpl(FoodRecordMapper foodRecordMapper) {
        this.foodRecordMapper = foodRecordMapper;
    }

    @Override
    public CalorieTrendVO getCalorieTrend(Long userId, int range) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(range - 1);

        // 查询指定范围的热量数据
        List<Map<String, Object>> dbTrend = foodRecordMapper.getDailyCalorieTrend(userId, startDate, today);

        // 转为 Map 方便补缺
        Map<LocalDate, Integer> dailyCalories = dbTrend.stream()
                .collect(Collectors.toMap(
                        m -> LocalDate.parse(m.get("date").toString()),
                        m -> ((Number) m.get("totalCalories")).intValue()
                ));

        // 构造连续数据（无数据的日期补 0）
        List<String> days = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 0; i < range; i++) {
            LocalDate date = startDate.plusDays(i);
            days.add(date.format(formatter));
            values.add(dailyCalories.getOrDefault(date, 0));
        }

        // 计算平均值
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        int avgCal = range > 0 ? sum / range : 0;

        // 计算趋势（比较后几天与前几天的均值）
        String trend = calculateTrend(values);

        CalorieTrendVO vo = new CalorieTrendVO();
        vo.setDays(days);
        vo.setValues(values);
        vo.setTarget(0); // 由前端根据用户信息动态计算
        vo.setAvgCal(avgCal);
        vo.setTrend(trend);
        return vo;
    }

    /**
     * 计算趋势描述
     */
    private String calculateTrend(List<Integer> values) {
        int size = values.size();
        if (size < 3) return "平稳";

        int half = size / 2;
        double firstHalfAvg = values.subList(0, half).stream().mapToInt(Integer::intValue).average().orElse(0);
        double secondHalfAvg = values.subList(half, size).stream().mapToInt(Integer::intValue).average().orElse(0);

        double diff = secondHalfAvg - firstHalfAvg;
        double threshold = firstHalfAvg * 0.1;

        if (Math.abs(diff) < threshold) return "平稳";
        return diff > 0 ? "上升" : "下降";
    }

    @Override
    public NutrientRatioVO getNutrientRatio(Long userId, LocalDate date) {
        // 查询当天的所有记录
        List<FoodRecord> records = foodRecordMapper.getRecordsByDate(userId, date);

        // 计算各类营养素总克数
        double totalProtein = 0, totalCarbs = 0, totalFat = 0;
        for (FoodRecord r : records) {
            totalProtein += Optional.ofNullable(r.getProtein()).orElse(0.0);
            totalCarbs += Optional.ofNullable(r.getCarbs()).orElse(0.0);
            totalFat += Optional.ofNullable(r.getFat()).orElse(0.0);
        }

        // 计算各类热量
        int proteinCal = (int) Math.round(totalProtein * PROTEIN_CAL);
        int carbsCal = (int) Math.round(totalCarbs * CARBS_CAL);
        int fatCal = (int) Math.round(totalFat * FAT_CAL);
        int totalCal = proteinCal + carbsCal + fatCal;

        NutrientRatioVO vo = new NutrientRatioVO();

        if (totalCal == 0) {
            vo.setProtein(emptyNutrientItem());
            vo.setCarbs(emptyNutrientItem());
            vo.setFat(emptyNutrientItem());
        } else {
            vo.setProtein(buildNutrientItem(proteinCal, totalCal));
            vo.setCarbs(buildNutrientItem(carbsCal, totalCal));
            vo.setFat(buildNutrientItem(fatCal, totalCal));
        }

        vo.setTotalCal(totalCal);
        vo.setTotalProtein(totalProtein);
        vo.setTotalCarbs(totalCarbs);
        vo.setTotalFat(totalFat);
        return vo;
    }

    private NutrientRatioVO.NutrientItem buildNutrientItem(int nutrientCal, int totalCal) {
        NutrientRatioVO.NutrientItem item = new NutrientRatioVO.NutrientItem();
        item.setCalories(nutrientCal);
        item.setPercent((int) Math.round((double) nutrientCal / totalCal * 100));
        return item;
    }

    private NutrientRatioVO.NutrientItem emptyNutrientItem() {
        NutrientRatioVO.NutrientItem item = new NutrientRatioVO.NutrientItem();
        item.setCalories(0);
        item.setPercent(0);
        return item;
    }
}
