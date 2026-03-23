package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for notice/announcement data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponse {

    /** Unique notice ID. */
    private Long id;

    /** Notice category (e.g., 特别通知, 通知, 安全通知, 站点通知). */
    private String category;

    /** Notice title. */
    private String title;

    /** Full notice content. */
    private String content;

    /** Date when the notice was published. */
    private LocalDate createTime;
}
