package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.AiAnalysisLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 分析日志 Mapper 接口
 */
@Mapper
public interface AiAnalysisLogMapper extends BaseMapper<AiAnalysisLog> {

    /**
     * 分页查询当前用户的分析记录（按创建时间倒序）
     */
    @Select("SELECT * FROM ai_analysis_log WHERE user_id = #{userId} " +
            "AND analysis_type = 'manual' " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<AiAnalysisLog> selectPageByUserId(@Param("userId") Long userId,
                                           @Param("offset") int offset,
                                           @Param("pageSize") int pageSize);

    /**
     * 统计当前用户的分析记录总数（仅手动分析）
     */
    @Select("SELECT COUNT(*) FROM ai_analysis_log WHERE user_id = #{userId} AND analysis_type = 'manual'")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 清空当前用户的所有分析记录
     */
    @Delete("DELETE FROM ai_analysis_log WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 查询用户最近一条相同食物名的分析记录（不限类型）
     */
    @Select("SELECT * FROM ai_analysis_log WHERE user_id = #{userId} AND food_name = #{foodName} " +
            "ORDER BY create_time DESC LIMIT 1")
    AiAnalysisLog findByUserIdAndFoodName(@Param("userId") Long userId,
                                           @Param("foodName") String foodName);
}
