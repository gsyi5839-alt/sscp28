package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for settlement detail data.
 * Represents detailed settlement statistics for a specific game type on a specific date.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementDetailResponse {

    /**
     * Transaction date in YYYY-MM-DD format.
     */
    private LocalDate date;

    /**
     * Game type code (e.g., PC28, BJ28).
     */
    private String gameType;

    /**
     * Display name of the game type.
     */
    private String gameName;

    /**
     * Total number of orders for this game type.
     */
    private Integer orderCount;

    /**
     * Total bet amount for this game type.
     */
    private BigDecimal betAmount;

    /**
     * Total valid bet amount (eligible for rebates).
     */
    private BigDecimal validAmount;

    /**
     * Total rebate amount earned for this game type.
     */
    private BigDecimal rebate;

    /**
     * Total win/loss amount for this game type (positive = win, negative = loss).
     */
    private BigDecimal winLoss;

    /**
     * Remarks or notes for this settlement record.
     */
    private String remark;
}
