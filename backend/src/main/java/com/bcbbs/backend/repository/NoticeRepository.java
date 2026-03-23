package com.bcbbs.backend.repository;

import com.bcbbs.backend.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Notice entity.
 * Provides database operations for system announcements.
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * Find all enabled notices ordered by creation time descending.
     *
     * @return list of enabled notices sorted by newest first
     */
    List<Notice> findByEnabledTrueOrderByCreateTimeDesc();

    /**
     * Find enabled notices by category, ordered by creation time descending.
     *
     * @param category the notice category
     * @return list of enabled notices in the specified category
     */
    List<Notice> findByCategoryAndEnabledTrueOrderByCreateTimeDesc(String category);
}
