package com.bcbbs.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BetRecord entity - stores individual bet/wager records.
 * Tracks both settled and unsettled bets for each user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bet_records", indexes = {
        @Index(name = "idx_bet_user_settled", columnList = "userId, settled"),
        @Index(name = "idx_bet_user_time", columnList = "userId, betTime")
})
public class BetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User who placed the bet. */
    @Column(nullable = false)
    private Long userId;

    /** Unique order number for the bet. */
    @Column(nullable = false, unique = true, length = 50)
    private String orderNo;

    /** Timestamp when the bet was placed. */
    @Column(nullable = false)
    private LocalDateTime betTime;

    /** Game type code (e.g., caPc28, aus10). */
    @Column(nullable = false, length = 50)
    private String gameType;

    /** Play method description (e.g., 两面盘-大). */
    @Column(nullable = false, length = 100)
    private String playMethod;

    /** Handicap/odds identifier (e.g., A盘, B盘). */
    @Column(length = 50)
    private String handicap;

    /** Amount wagered on this bet. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal betAmount;

    /** Rebate amount for this bet. */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal rebate = BigDecimal.ZERO;

    /** Potential win amount if the bet wins. */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal canWin = BigDecimal.ZERO;

    /** Whether this bet has been settled. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean settled = false;
}
