package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response DTO for bet records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetPageResponse {

    /** List of bet records for the current page. */
    private List<BetRecordResponse> list;

    /** Current page number (1-based). */
    private int page;

    /** Page size. */
    private int size;

    /** Total number of matching records. */
    private long total;
}
