package com.health.controller;

import com.health.common.PageResult;
import com.health.common.Result;
import com.health.dto.AiAnalyzeDietRequest;
import com.health.dto.AiAnalyzeManualRequest;
import com.health.service.AiNutritionService;
import com.health.vo.AiAnalyzeDietVO;
import com.health.vo.AiAnalyzeManualVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * AI 智能分析控制器
 * 提供食物营养分析、饮食评价和历史管理功能
 */
@RestController
@RequestMapping("/api/ai")
@Api(tags = "AI 智能分析")
public class AiController {

    private final AiNutritionService aiNutritionService;

    public AiController(AiNutritionService aiNutritionService) {
        this.aiNutritionService = aiNutritionService;
    }

    /**
     * 手动输入分析食物营养
     */
    @PostMapping("/analyze/manual")
    @ApiOperation("手动分析食物营养")
    public Result<AiAnalyzeManualVO> analyzeManual(HttpServletRequest request,
                                                    @Validated @RequestBody AiAnalyzeManualRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        AiAnalyzeManualVO vo = aiNutritionService.analyzeManual(userId, req.getFoodName(), req.getModel());
        return Result.success(vo);
    }

    /**
     * 饮食记录分析
     */
    @PostMapping("/analyze/diet")
    @ApiOperation("分析饮食记录")
    public Result<AiAnalyzeDietVO> analyzeDiet(HttpServletRequest request,
                                                @Validated @RequestBody AiAnalyzeDietRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        AiAnalyzeDietVO vo = aiNutritionService.analyzeDiet(userId, req.getDate(), req.getModel());
        return Result.success(vo);
    }

    /**
     * 根据用户画像生成个性化饮食建议
     */
    @GetMapping("/profile-tip")
    @ApiOperation("用户画像饮食建议")
    public Result<String> profileTip(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }
        String tip = aiNutritionService.generateProfileTip(userId);
        return Result.success(tip);
    }

    /**
     * 查询分析历史（分页）
     */
    @GetMapping("/analyze/history")
    @ApiOperation("查询分析历史")
    public Result<PageResult<AiAnalyzeManualVO>> history(HttpServletRequest request,
                                                          @RequestParam(defaultValue = "1")
                                                          @ApiParam("页码") int page,
                                                          @RequestParam(defaultValue = "10")
                                                          @ApiParam("每页条数") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        PageResult<AiAnalyzeManualVO> result = aiNutritionService.getHistory(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 删除单条分析记录
     */
    @DeleteMapping("/analyze/{id}")
    @ApiOperation("删除分析记录")
    public Result<Void> deleteById(HttpServletRequest request,
                                    @PathVariable @ApiParam("记录ID") Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        aiNutritionService.deleteById(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 清空当前用户的所有分析记录
     */
    @DeleteMapping("/analyze/history")
    @ApiOperation("清空分析历史")
    public Result<Void> clearHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        aiNutritionService.clearHistory(userId);
        return Result.success("已清空", null);
    }
}
