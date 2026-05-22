package com.poptrade.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页响应体，所有分页查询接口统一使用此类型返回。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总记录数 */
    private Long total;
    /** 本页数据 */
    private List<T> list;
    /** 当前页码 */
    private Integer pageNum;
    /** 每页条数 */
    private Integer pageSize;
    /** 总页数 */
    private Integer pages;

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L, Collections.emptyList(), 1, 10, 0);
    }

    public static <T> PageResult<T> of(Long total, List<T> list, Integer pageNum, Integer pageSize) {
        int pages = (int) Math.ceil((double) total / pageSize);
        return new PageResult<>(total, list, pageNum, pageSize, pages);
    }

    /**
     * 从 MyBatis-Plus 分页结果构建，并转换数据类型（Entity → VO）。
     *
     * @param page    MP 分页结果
     * @param mapper  Entity → VO 转换函数
     * @param <E>     Entity 类型
     * @param <V>     VO 类型
     */
    public static <E, V> PageResult<V> from(com.baomidou.mybatisplus.extension.plugins.pagination.Page<E> page,
                                            Function<E, V> mapper) {
        List<V> voList = page.getRecords().stream().map(mapper).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), voList, (int) page.getCurrent(), (int) page.getSize());
    }
}
