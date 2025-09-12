package co.com.pragma.model;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class DataPage<T> {
    private List<T> content;
    private int currentPage;
    private long totalElements;
    private int totalPages;
}