package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a single bet record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetRecordResponse {

    /** Unique order number. */
    private String orderNo;

    /** Timestamp when the bet was placed. */
    private LocalDateTime time;

    /** Game type code. */
    private String type;

    /** Play method description. */
    private String playMethod;

    /** Handicap/odds identifier. */
    private String handicap;

    /** Amount wagered. */
    private BigDecimal betAmount;

    /** Rebate amount. */
    private BigDecimal rebate;

    /** Potential win amount. */
    private BigDecimal canWin;
}
