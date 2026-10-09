package com.carddemo.web.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Pagination envelope replacing the CICS STARTBR / READNEXT browse of the list screens. */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <S, T> PageResponse<T> of(Page<S> page, java.util.function.Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
