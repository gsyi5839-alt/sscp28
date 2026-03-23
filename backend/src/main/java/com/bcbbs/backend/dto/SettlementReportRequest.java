package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for querying settlement report details.
 * Used to get detailed settlement data for a specific date.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementReportRequest {

    /**
     * The date for which to retrieve settlement details (required).
     * Format: YYYY-MM-DD
     */
    private LocalDate date;
}
