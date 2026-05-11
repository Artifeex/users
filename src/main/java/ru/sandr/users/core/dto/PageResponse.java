package ru.sandr.users.core.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Schema(description = "Unified paginated response wrapper")
public class PageResponse<T> {
    @ArraySchema(arraySchema = @Schema(description = "Page items"))
    private List<T> items;
    @Schema(description = "Zero-based page index", example = "0")
    private int page;
    @Schema(description = "Requested page size", example = "20")
    private int size;
    @Schema(description = "Total items amount", example = "135")
    private long totalElements;
    @Schema(description = "Total pages amount", example = "7")
    private int totalPages;
    @Schema(description = "Is next page available", example = "true")
    private boolean hasNext;

    public PageResponse(Page<T> page) {
        this.items = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.hasNext = page.hasNext();
    }
}
