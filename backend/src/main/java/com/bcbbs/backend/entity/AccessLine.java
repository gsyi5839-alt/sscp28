package com.bcbbs.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "access_lines")
public class AccessLine {

    public enum LineType {
        MEMBER,
        AGENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Display name of the line
    @Column(nullable = false, length = 100)
    private String name;

    // Target URL for this line
    @Column(nullable = false, length = 500)
    private String url;

    // Line category used by the frontend tabs
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LineType type;

    // Whether the line is available to users
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // Sort order for display
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // Latest measured latency in milliseconds (optional)
    @Column(name = "last_ping_ms")
    private Integer lastPingMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

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

