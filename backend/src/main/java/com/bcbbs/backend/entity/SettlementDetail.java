package com.bcbbs.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Settlement detail entity - stores detailed settlement data per game type per day.
 * Multiple records per user per date, one for each game type played.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "settlement_details")
public class SettlementDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Associated user ID - references users table
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Transaction date - the date for which this record contains settlement data
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    /**
     * Game type code (e.g., PC28, BJ28, etc.)
     */
    @Column(name = "game_type", nullable = false, length = 50)
    private String gameType;

    /**
     * Display name of the game type
     */
    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    /**
     * Total number of orders for this game type on this date
     */
    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 0;

    /**
     * Total bet amount for this game type
     */
    @Column(name = "bet_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal betAmount = BigDecimal.ZERO;

    /**
     * Total valid bet amount (eligible for rebates) for this game type
     */
    @Column(name = "valid_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal validAmount = BigDecimal.ZERO;

    /**
     * Total rebate amount earned for this game type
     */
    @Column(name = "rebate", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal rebate = BigDecimal.ZERO;

    /**
     * Total win/loss amount for this game type (positive = win, negative = loss)
     */
    @Column(name = "win_loss", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal winLoss = BigDecimal.ZERO;

    /**
     * Remarks or notes for this settlement record
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * Record creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record last update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
