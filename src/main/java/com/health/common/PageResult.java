package com.health.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页查询结果
 *
 * @param <T> 数据类型
 */
@Data
@ApiModel("分页结果")
public class PageResult<T> {

    @ApiModelProperty("数据列表")
    private List<T> list;

    @ApiModelProperty("总记录数")
    private long total;

    @ApiModelProperty("当前页")
    private int page;

    @ApiModelProperty("每页条数")
    private int pageSize;

    @ApiModelProperty("总页数")
    private long pages;

    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.pages = (long) Math.ceil((double) total / pageSize);
    }
}
