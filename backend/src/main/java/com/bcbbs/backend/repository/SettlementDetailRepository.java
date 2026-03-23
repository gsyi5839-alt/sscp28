package com.bcbbs.backend.repository;

import com.bcbbs.backend.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SettlementDetail entity - handles detailed settlement data per game type.
 */
@Repository
public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

    /**
     * Find settlement detail by user ID, transaction date and game type.
     *
     * @param userId the user ID
     * @param transactionDate the transaction date
     * @param gameType the game type code
     * @return Optional containing the settlement detail if found
     */
    Optional<SettlementDetail> findByUserIdAndTransactionDateAndGameType(
            Long userId, LocalDate transactionDate, String gameType);

    /**
     * Find all settlement details for a user on a specific date.
     *
     * @param userId the user ID
     * @param transactionDate the transaction date
     * @return list of settlement details ordered by game type
     */
    List<SettlementDetail> findByUserIdAndTransactionDateOrderByGameTypeAsc(
            Long userId, LocalDate transactionDate);

    /**
     * Find settlement details for a user within a date range.
     *
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of settlement details
     */
    List<SettlementDetail> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescGameTypeAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find all settlement details for a specific date across all users.
     * Used for administrative reports.
     *
     * @param transactionDate the transaction date
     * @return list of settlement details
     */
    List<SettlementDetail> findByTransactionDateOrderByUserIdAsc(LocalDate transactionDate);

    /**
     * Check if settlement detail exists for user, date and game type.
     *
     * @param userId the user ID
     * @param transactionDate the transaction date
     * @param gameType the game type code
     * @return true if record exists
     */
    boolean existsByUserIdAndTransactionDateAndGameType(
            Long userId, LocalDate transactionDate, String gameType);

    /**
     * Delete settlement details for a user on a specific date.
     * Used when recalculating daily settlements.
     *
     * @param userId the user ID
     * @param transactionDate the transaction date
     */
    void deleteByUserIdAndTransactionDate(Long userId, LocalDate transactionDate);

    /**
     * Get summary statistics for a user on a specific date.
     *
     * @param userId the user ID
     * @param transactionDate the transaction date
     * @return list of settlement details
     */
    @Query("SELECT sd FROM SettlementDetail sd WHERE sd.userId = :userId AND sd.transactionDate = :date ORDER BY sd.gameType")
    List<SettlementDetail> getDailySummaryByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate transactionDate);
}
