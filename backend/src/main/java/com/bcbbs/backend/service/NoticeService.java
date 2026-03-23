package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.NoticeResponse;
import com.bcbbs.backend.entity.Notice;
import com.bcbbs.backend.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing system notices/announcements.
 * Provides grouped notice data with caching support.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * Predefined notice category display order.
     */
    private static final List<String> CATEGORY_ORDER = List.of(
            "特别通知", "通知", "安全通知", "站点通知"
    );

    /**
     * Get all enabled notices grouped by category.
     * Results are cached for 6 hours to reduce database load.
     *
     * @return map of category name to list of notice responses
     */
    @Cacheable(value = "notices")
    @Transactional(readOnly = true)
    public Map<String, List<NoticeResponse>> getAllNoticesGrouped() {
        log.debug("Fetching all enabled notices from database");

        List<Notice> allNotices = noticeRepository.findByEnabledTrueOrderByCreateTimeDesc();

        // Group by category
        Map<String, List<NoticeResponse>> grouped = allNotices.stream()
                .collect(Collectors.groupingBy(
                        Notice::getCategory,
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));

        // Maintain predefined category order, include empty categories
        Map<String, List<NoticeResponse>> ordered = new LinkedHashMap<>();
        for (String category : CATEGORY_ORDER) {
            ordered.put(category, grouped.getOrDefault(category, List.of()));
        }
        // Add any extra categories not in the predefined order
        grouped.forEach((key, value) -> ordered.putIfAbsent(key, value));

        return ordered;
    }

    /**
     * Convert Notice entity to NoticeResponse DTO.
     *
     * @param notice the notice entity
     * @return the notice response DTO
     */
    private NoticeResponse toResponse(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .category(notice.getCategory())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createTime(notice.getCreateTime())
                .build();
    }
}
