package com.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.health.dto.FoodRecordDTO;
import com.health.entity.FoodRecord;
import com.health.mapper.FoodRecordMapper;
import com.health.service.FoodRecordService;
import com.health.vo.FoodRecordListVO;
import com.health.vo.NutritionSummaryVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 饮食记录服务实现类
 */
@Service
public class FoodRecordServiceImpl extends ServiceImpl<FoodRecordMapper, FoodRecord> implements FoodRecordService {

    private final FoodRecordMapper foodRecordMapper;

    public FoodRecordServiceImpl(FoodRecordMapper foodRecordMapper) {
        this.foodRecordMapper = foodRecordMapper;
    }

    @Override
    public Long addRecord(Long userId, FoodRecordDTO dto) {
        FoodRecord record = new FoodRecord();
        record.setUserId(userId);
        record.setFoodName(dto.getFoodName());
        record.setCalories(dto.getCalories());
        record.setProtein(dto.getProtein());
        record.setCarbs(dto.getCarbs());
        record.setFat(dto.getFat());
        record.setMealType(dto.getMealType());
        record.setRecordDate(dto.getRecordDate());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(record);
        return record.getId();
    }

    @Override
    public FoodRecordListVO getRecordList(Long userId, LocalDate recordDate) {
        // 查询记录列表
        List<FoodRecord> records = foodRecordMapper.selectList(
                new LambdaQueryWrapper<FoodRecord>()
                        .eq(FoodRecord::getUserId, userId)
                        .eq(FoodRecord::getRecordDate, recordDate)
                        .orderByAsc(FoodRecord::getCreateTime)
        );

        // 查询当日营养汇总
        Map<String, Object> summary = foodRecordMapper.getDateNutritionSummary(userId, recordDate);
        NutritionSummaryVO nutritionSummary = buildNutritionSummary(summary);

        FoodRecordListVO vo = new FoodRecordListVO();
        vo.setRecords(records);
        vo.setNutritionSummary(nutritionSummary);
        return vo;
    }

    /**
     * 构建营养汇总对象
     */
    private NutritionSummaryVO buildNutritionSummary(Map<String, Object> summary) {
        NutritionSummaryVO vo = new NutritionSummaryVO();
        vo.setTotalCal(toDouble(summary.get("totalCal")));
        vo.setTotalProtein(toDouble(summary.get("totalProtein")));
        vo.setTotalCarbs(toDouble(summary.get("totalCarbs")));
        vo.setTotalFat(toDouble(summary.get("totalFat")));
        // 目标热量和进度可在前端计算，后端默认返回空
        vo.setTargetCal(0.0);
        vo.setProgress(0.0);
        return vo;
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }

    @Override
    public void updateRecord(Long id, Long userId, FoodRecordDTO dto) {
        FoodRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("饮食记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作他人的饮食记录");
        }

        record.setFoodName(dto.getFoodName());
        record.setCalories(dto.getCalories());
        record.setProtein(dto.getProtein());
        record.setCarbs(dto.getCarbs());
        record.setFat(dto.getFat());
        record.setMealType(dto.getMealType());
        record.setRecordDate(dto.getRecordDate());
        record.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(record);
    }

    @Override
    public void deleteRecord(Long id, Long userId) {
        FoodRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("饮食记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人的饮食记录");
        }

        baseMapper.deleteById(id);
    }
}
