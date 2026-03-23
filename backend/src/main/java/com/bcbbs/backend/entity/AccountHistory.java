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
 * Account history entity - stores daily aggregated transaction statistics for each user.
 * One record per user per day, automatically updated when bets are settled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_history")
public class AccountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Associated user ID - references users table
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Transaction date - the date for which this record aggregates data
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    /**
     * Total number of orders placed on this date
     */
    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 0;

    /**
     * Total bet amount for all orders on this date
     */
    @Column(name = "bet_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal betAmount = BigDecimal.ZERO;

    /**
     * Total valid bet amount (eligible for rebates) on this date
     */
    @Column(name = "valid_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal validAmount = BigDecimal.ZERO;

    /**
     * Total rebate amount earned on this date
     */
    @Column(name = "rebate", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal rebate = BigDecimal.ZERO;

    /**
     * Total win/loss amount on this date (positive = win, negative = loss)
     */
    @Column(name = "win_loss", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal winLoss = BigDecimal.ZERO;

    /**
     * Settlement status: PENDING - not yet settled, SETTLED - settled
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

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

    public enum SettlementStatus {
        PENDING,    // Not yet settled, can be modified
        SETTLED     // Settlement completed, read-only
    }
}
