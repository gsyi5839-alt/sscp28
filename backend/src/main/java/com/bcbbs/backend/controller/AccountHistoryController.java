package com.bcbbs.backend.controller;

import com.bcbbs.backend.dto.AccountHistoryRequest;
import com.bcbbs.backend.dto.AccountHistoryResponse;
import com.bcbbs.backend.dto.ApiResponse;
import com.bcbbs.backend.dto.SettlementDetailResponse;
import com.bcbbs.backend.dto.SettlementReportRequest;
import com.bcbbs.backend.entity.User;
import com.bcbbs.backend.service.AccountHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Account history controller - provides APIs for retrieving user transaction history and settlement reports.
 *
 * Endpoints:
 *   GET /api/account/history - Get last two weeks account history
 *   POST /api/account/history/query - Query account history by date range
 *   POST /api/account/settlement/report - Get settlement report for a specific date
 */
@Slf4j
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountHistoryController {

    private final AccountHistoryService accountHistoryService;

    /**
     * Get account history for the last two weeks (last week + this week).
     * Returns daily aggregated statistics for the current user.
     *
     * @param user the authenticated user
     * @return list of account history responses
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AccountHistoryResponse>>> getLastTwoWeeksHistory(
            @AuthenticationPrincipal User user) {
        log.debug("Getting last two weeks account history for user: {}", user.getId());

        List<AccountHistoryResponse> history = accountHistoryService.getLastTwoWeeksHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Query account history by date range or specific dates.
     *
     * @param user the authenticated user
     * @param request the query request containing date range or specific dates
     * @return list of account history responses matching the criteria
     */
    @PostMapping("/history/query")
    public ResponseEntity<ApiResponse<List<AccountHistoryResponse>>> queryAccountHistory(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AccountHistoryRequest request) {
        log.debug("Querying account history for user: {} with request: {}", user.getId(), request);

        List<AccountHistoryResponse> history = accountHistoryService.getAccountHistoryByDateRange(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Get settlement report for a specific date.
     * Returns detailed breakdown by game type.
     *
     * @param user the authenticated user
     * @param request the settlement report request containing the date
     * @return list of settlement detail responses
     */
    @PostMapping("/settlement/report")
    public ResponseEntity<ApiResponse<List<SettlementDetailResponse>>> getSettlementReport(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SettlementReportRequest request) {
        log.debug("Getting settlement report for user: {} on date: {}", user.getId(), request.getDate());

        List<SettlementDetailResponse> report = accountHistoryService.getSettlementReport(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
