package com.health.utils;

import com.health.entity.FoodRecord;
import com.health.entity.User;
import com.health.vo.NutritionSummaryVO;

import java.util.List;
import java.util.Optional;

/**
 * 营养计算工具类
 */
public class NutritionCalculator {

    private NutritionCalculator() {}

    /**
     * 从饮食记录列表计算营养汇总
     */
    public static NutritionSummaryVO calculateSummary(List<FoodRecord> records) {
        double totalCal = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
        for (FoodRecord r : records) {
            totalCal += Optional.ofNullable(r.getCalories()).orElse(0.0);
            totalProtein += Optional.ofNullable(r.getProtein()).orElse(0.0);
            totalCarbs += Optional.ofNullable(r.getCarbs()).orElse(0.0);
            totalFat += Optional.ofNullable(r.getFat()).orElse(0.0);
        }
        NutritionSummaryVO summary = new NutritionSummaryVO();
        summary.setTotalCal(totalCal);
        summary.setTotalProtein(totalProtein);
        summary.setTotalCarbs(totalCarbs);
        summary.setTotalFat(totalFat);
        return summary;
    }

    /**
     * 使用 Mifflin-St Jeor 公式计算每日推荐热量摄入
     */
    public static int calcTargetCalories(User user) {
        if (user == null || user.getWeight() == null || user.getHeight() == null || user.getAge() == null) {
            return 0;
        }
        double w = user.getWeight();
        double h = user.getHeight();
        double a = user.getAge();
        int gender = user.getGender() != null ? user.getGender() : 0;

        double bmr;
        if (gender == 1) {
            bmr = 10 * w + 6.25 * h - 5 * a + 5;
        } else if (gender == 2) {
            bmr = 10 * w + 6.25 * h - 5 * a - 161;
        } else {
            bmr = 10 * w + 6.25 * h - 5 * a;
        }

        double tdee = bmr * 1.375; // 轻度活动 PAL

        String goal = user.getGoal();
        if ("lose".equals(goal)) {
            tdee *= 0.8;
        } else if ("gain".equals(goal)) {
            tdee *= 1.15;
        }

        return (int) Math.round(tdee);
    }
}
