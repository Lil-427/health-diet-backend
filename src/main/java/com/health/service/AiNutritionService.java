package com.health.service;

import com.health.common.PageResult;
import com.health.vo.AiAnalyzeDietVO;
import com.health.vo.AiAnalyzeManualVO;

import java.time.LocalDate;

/**
 * AI 营养分析服务接口
 */
public interface AiNutritionService {

    /**
     * 手动输入分析食物营养
     *
     * @param userId   用户 ID
     * @param foodName 食物名称
     * @param model    模型名称（预留）
     * @return 手动分析结果
     */
    AiAnalyzeManualVO analyzeManual(Long userId, String foodName, String model);

    /**
     * 饮食记录分析
     *
     * @param userId 用户 ID
     * @param date   分析日期
     * @param model  模型名称（预留）
     * @return 饮食分析结果
     */
    AiAnalyzeDietVO analyzeDiet(Long userId, LocalDate date, String model);

    /**
     * 查询分析历史（分页）
     *
     * @param userId   用户 ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<AiAnalyzeManualVO> getHistory(Long userId, int page, int pageSize);

    /**
     * 删除单条分析记录
     *
     * @param userId 用户 ID
     * @param id     记录 ID
     */
    void deleteById(Long userId, Long id);

    /**
     * 根据用户画像生成个性化饮食建议
     *
     * @param userId 用户 ID
     * @return AI 生成的个性化建议
     */
    String generateProfileTip(Long userId);

    /**
     * 清空当前用户的所有分析记录
     *
     * @param userId 用户 ID
     */
    void clearHistory(Long userId);
}
