package com.coursebuddy.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

/**
 * 组件
 */
public final class MybatisPlusPageUtils {

    private MybatisPlusPageUtils() {
    }

    public static <T> Page<T> toMpPage(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new Page<>(1, 1000);
        }
        return new Page<>(pageable.getPageNumber() + 1L, pageable.getPageSize());
    }

    public static <T> org.springframework.data.domain.Page<T> toSpringPage(IPage<T> mpPage, Pageable pageable) {
        List<T> records = mpPage == null || mpPage.getRecords() == null
                ? Collections.emptyList()
                : mpPage.getRecords();
        if (pageable == null || pageable.isUnpaged()) {
            return new PageImpl<>(records);
        }
        return new PageImpl<>(records, pageable, mpPage.getTotal());
    }
}
