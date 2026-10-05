package com.resume.marketplace.common;

import java.util.List;

/**
 * 通用分页结果对象。
 *
 * <p>分页查询统一返回该结构，前端可据此渲染分页条：</p>
 * <pre>{ list: [...], total: 50, page: 1, size: 10, pages: 5 }</pre>
 */
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> list;
    /** 符合条件的总记录数（用于计算总页数） */
    private long total;
    /** 当前页码（从 1 开始） */
    private int page;
    /** 每页条数 */
    private int size;
    /** 总页数 */
    private int pages;

    public PageResult(List<T> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        // 计算总页数：向上取整，size 至少为 1（避免除零）
        this.pages = (int) ((total + size - 1) / Math.max(size, 1));
    }

    public List<T> getList() {
        return list;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getPages() {
        return pages;
    }
}