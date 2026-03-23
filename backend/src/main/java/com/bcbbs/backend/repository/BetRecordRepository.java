package com.bcbbs.backend.repository;

import com.bcbbs.backend.entity.BetRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for BetRecord entity.
 * Provides database operations for bet/wager records.
 */
@Repository
public interface BetRecordRepository extends JpaRepository<BetRecord, Long> {

    /**
     * Find unsettled bet records for a specific user with pagination.
     *
     * @param userId the user ID
     * @param settled false to get unsettled bets
     * @param pageable pagination parameters
     * @return page of unsettled bet records
     */
    Page<BetRecord> findByUserIdAndSettledOrderByBetTimeDesc(
            Long userId, Boolean settled, Pageable pageable);
}
