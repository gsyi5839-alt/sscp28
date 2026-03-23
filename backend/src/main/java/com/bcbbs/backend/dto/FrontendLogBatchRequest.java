package com.bcbbs.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch frontend log request DTO
 * Receives multiple log entries in a single request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontendLogBatchRequest {

    /**
     * List of log entries
     */
    @NotEmpty(message = "Logs list cannot be empty")
    @Size(max = 100, message = "Maximum 100 logs per batch")
    @Valid
    private List<FrontendLogEntry> logs;
}
