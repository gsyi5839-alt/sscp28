package com.bcbbs.backend.controller;

import com.bcbbs.backend.dto.ApiResponse;
import com.bcbbs.backend.dto.BetPageResponse;
import com.bcbbs.backend.entity.User;
import com.bcbbs.backend.service.BetRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bet record controller - provides APIs for querying bet/wager records.
 *
 * Endpoints:
 *   GET /api/bet/unsettled - Get paginated unsettled bets for the authenticated user
 */
@Slf4j
@RestController
@RequestMapping("/api/bet")
@RequiredArgsConstructor
public class BetRecordController {

    private final BetRecordService betRecordService;

    /**
     * Get paginated unsettled bet records for the authenticated user.
     *
     * @param user the authenticated user (injected by Spring Security)
     * @param page page number (1-based, default 1)
     * @param size page size (default 20, max 50)
     * @return paginated bet records response
     */
    @GetMapping("/unsettled")
    public ResponseEntity<ApiResponse<BetPageResponse>> getUnsettledBets(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        // Validate pagination parameters
        if (page < 1) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Page must be greater than 0"));
        }
        if (size < 1 || size > 50) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Size must be between 1 and 50"));
        }

        log.debug("Getting unsettled bets for user: {}, page: {}, size: {}", user.getId(), page, size);
        BetPageResponse response = betRecordService.getUnsettledBets(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
