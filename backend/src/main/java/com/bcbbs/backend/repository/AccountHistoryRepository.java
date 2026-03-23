package com.bcbbs.backend.repository;

import com.bcbbs.backend.entity.AccountHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AccountHistory entity - handles daily aggregated transaction statistics.
 */
@Repository
public interface AccountHistoryRepository extends JpaRepository<AccountHistory, Long> {

    /**
     * Find account history record by user ID and transaction date.
     *
     * @param userId the user ID
     * @param transactionDate the transaction date
     * @return Optional containing the account history if found
     */
    Optional<AccountHistory> findByUserIdAndTransactionDate(Long userId, LocalDate transactionDate);

    /**
     * Find all account history records for a user within a date range.
     *
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of account history records ordered by date descending
     */
    List<AccountHistory> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find account history records for a user for specific dates.
     *
     * @param userId the user ID
     * @param dates list of dates to query
     * @return list of account history records
     */
    @Query("SELECT ah FROM AccountHistory ah WHERE ah.userId = :userId AND ah.transactionDate IN :dates ORDER BY ah.transactionDate DESC")
    List<AccountHistory> findByUserIdAndTransactionDates(
            @Param("userId") Long userId,
            @Param("dates") List<LocalDate> dates);

    /**
     * Check if account history exists for user and date.
     *
     * @param userId the user ID
     * @param transactionDate the transaction date
     * @return true if record exists
     */
    boolean existsByUserIdAndTransactionDate(Long userId, LocalDate transactionDate);

    /**
     * Delete account history records for a user within a date range.
     * Used for data cleanup or recalculation.
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     */
    void deleteByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
