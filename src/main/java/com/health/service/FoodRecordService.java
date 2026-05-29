package com.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.health.dto.FoodRecordDTO;
import com.health.entity.FoodRecord;
import com.health.vo.FoodRecordListVO;

import java.time.LocalDate;

/**
 * 饮食记录服务接口
 */
public interface FoodRecordService extends IService<FoodRecord> {

    /**
     * 新增饮食记录
     *
     * @param userId 用户 ID
     * @param dto    饮食记录数据
     * @return 记录 ID
     */
    Long addRecord(Long userId, FoodRecordDTO dto);

    /**
     * 查询某天的饮食记录列表（含营养汇总）
     *
     * @param userId     用户 ID
     * @param recordDate 记录日期
     * @return 饮食记录列表 VO（含营养汇总）
     */
    FoodRecordListVO getRecordList(Long userId, LocalDate recordDate);

    /**
     * 更新饮食记录
     *
     * @param id     记录 ID
     * @param userId 用户 ID（用于权限校验）
     * @param dto    更新数据
     */
    void updateRecord(Long id, Long userId, FoodRecordDTO dto);

    /**
     * 删除饮食记录
     *
     * @param id     记录 ID
     * @param userId 用户 ID（用于权限校验）
     */
    void deleteRecord(Long id, Long userId);
}
