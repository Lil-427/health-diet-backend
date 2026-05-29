package com.health.controller;

import com.health.common.Result;
import com.health.dto.FoodRecordDTO;
import com.health.service.FoodRecordService;
import com.health.vo.FoodRecordListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

/**
 * 饮食记录控制器
 * 提供饮食记录的增删改查功能
 */
@RestController
@RequestMapping("/api/food/record")
@Api(tags = "饮食记录管理")
public class FoodRecordController {

    private final FoodRecordService foodRecordService;

    public FoodRecordController(FoodRecordService foodRecordService) {
        this.foodRecordService = foodRecordService;
    }

    /**
     * 新增饮食记录
     */
    @PostMapping
    @ApiOperation("新增饮食记录")
    public Result<Long> addRecord(HttpServletRequest request,
                                   @Validated @RequestBody FoodRecordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        Long recordId = foodRecordService.addRecord(userId, dto);
        return Result.success("添加成功", recordId);
    }

    /**
     * 查询某天的饮食记录列表（含当日营养汇总）
     *
     * @param date 记录日期（格式：yyyy-MM-dd）
     */
    @GetMapping("/list")
    @ApiOperation("查询某天的饮食记录")
    public Result<FoodRecordListVO> getRecordList(HttpServletRequest request,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                   @ApiParam(value = "记录日期", example = "2026-05-25") LocalDate date) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        FoodRecordListVO vo = foodRecordService.getRecordList(userId, date);
        return Result.success(vo);
    }

    /**
     * 更新饮食记录
     */
    @PutMapping("/{id}")
    @ApiOperation("更新饮食记录")
    public Result<Void> updateRecord(HttpServletRequest request,
                                      @PathVariable @ApiParam(value = "记录 ID", example = "1") Long id,
                                      @Validated @RequestBody FoodRecordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        foodRecordService.updateRecord(id, userId, dto);
        return Result.success("更新成功", null);
    }

    /**
     * 删除饮食记录
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除饮食记录")
    public Result<Void> deleteRecord(HttpServletRequest request,
                                      @PathVariable @ApiParam(value = "记录 ID", example = "1") Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        foodRecordService.deleteRecord(id, userId);
        return Result.success("删除成功", null);
    }
}
