package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for account history data.
 * Represents a single day's aggregated transaction statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryResponse {

    /**
     * Transaction date in YYYY-MM-DD format.
     */
    private LocalDate date;

    /**
     * Day of week in Chinese (周一, 周二, etc.).
     */
    private String weekday;

    /**
     * Total number of orders placed on this date.
     */
    private Integer orderCount;

    /**
     * Total bet amount for all orders.
     */
    private BigDecimal betAmount;

    /**
     * Total valid bet amount (eligible for rebates).
     */
    private BigDecimal validAmount;

    /**
     * Total rebate amount earned.
     */
    private BigDecimal rebate;

    /**
     * Total win/loss amount (positive = win, negative = loss).
     */
    private BigDecimal winLoss;

    /**
     * Settlement status: PENDING or SETTLED.
     */
    private String settlementStatus;
}
