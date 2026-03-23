package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for querying account history.
 * Used to filter account history records by date range.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryRequest {

    /**
     * Start date for the query range (inclusive).
     * If not provided, defaults to 14 days ago.
     */
    private LocalDate startDate;

    /**
     * End date for the query range (inclusive).
     * If not provided, defaults to today.
     */
    private LocalDate endDate;

    /**
     * Specific dates to query (alternative to date range).
     * Used when querying specific days like last week + this week.
     */
    private java.util.List<LocalDate> dates;
}
