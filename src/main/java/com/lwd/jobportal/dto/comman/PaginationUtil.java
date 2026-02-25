package com.lwd.jobportal.dto.comman;

import java.util.List;
import org.springframework.data.domain.Page;

public final class PaginationUtil {

    private PaginationUtil() {}

    public static <T, R> PagedResponse<R> buildPagedResponse(
            Page<T> page,
            List<R> content
    ) {
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
