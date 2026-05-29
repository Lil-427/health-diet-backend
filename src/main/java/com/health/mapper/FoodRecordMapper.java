package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.FoodRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 饮食记录 Mapper 接口
 */
@Mapper
public interface FoodRecordMapper extends BaseMapper<FoodRecord> {

    /**
     * 近7日每日总热量统计
     */
    @Select("SELECT record_date AS date, SUM(calories) AS totalCalories " +
            "FROM food_record " +
            "WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY record_date " +
            "ORDER BY record_date")
    List<Map<String, Object>> getDailyCalorieTrend(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 查询某天所有饮食记录
     */
    @Select("SELECT * FROM food_record WHERE user_id = #{userId} AND record_date = #{recordDate} ORDER BY create_time ASC")
    List<FoodRecord> getRecordsByDate(@Param("userId") Long userId,
                                      @Param("recordDate") LocalDate recordDate);

    /**
     * 查询某天营养汇总
     */
    @Select("SELECT " +
            "COALESCE(SUM(calories), 0) AS totalCal, " +
            "COALESCE(SUM(protein), 0) AS totalProtein, " +
            "COALESCE(SUM(carbs), 0) AS totalCarbs, " +
            "COALESCE(SUM(fat), 0) AS totalFat " +
            "FROM food_record WHERE user_id = #{userId} AND record_date = #{date}")
    Map<String, Object> getDateNutritionSummary(@Param("userId") Long userId,
                                                @Param("date") LocalDate date);
}
