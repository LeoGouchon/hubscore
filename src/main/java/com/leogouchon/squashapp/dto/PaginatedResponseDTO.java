package com.leogouchon.squashapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginatedResponseDTO<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;

    /**
     * @deprecated (since 1.0.0, forRemoval=true, do not use this constructor)
      */
    @Deprecated(since="1.0.0", forRemoval=true)
    public PaginatedResponseDTO() {}

    public PaginatedResponseDTO(List<T> content, int currentPage, int totalPages, long totalElements, int pageSize) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
    }
}
