package com.bcbbs.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Notice entity - stores system announcements and notifications.
 * Categories: 特别通知, 通知, 安全通知, 站点通知
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Notice category (e.g., 特别通知, 通知, 安全通知, 站点通知).
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * Notice title displayed in the notice list.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Full notice content text.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Date when the notice was created/published.
     */
    @Column(nullable = false)
    private LocalDate createTime;

    /**
     * Whether the notice is currently visible to users.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
}
