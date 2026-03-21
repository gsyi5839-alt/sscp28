package com.bcbbs.backend.controller;

import com.bcbbs.backend.dto.ApiResponse;
import com.bcbbs.backend.dto.LotteryGameOption;
import com.bcbbs.backend.dto.LotteryInfoResponse;
import com.bcbbs.backend.dto.LotteryListResponse;
import com.bcbbs.backend.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public controller that proxies lottery data from the upstream bw1284.cc API.
 *
 * Endpoints:
 *   GET /api/public/lottery/info?lotCode=720
 *   GET /api/public/lottery/list?lotCode=720&pageNo=1&pageSize=30
 *   GET /api/public/lottery/games
 */
@RestController
@RequestMapping("/api/public/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    /**
     * Get current issue info: latest draw result + next draw time.
     *
     * @param lotCode lottery code (e.g. 720 = 加拿大PC28)
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<LotteryInfoResponse>> getInfo(
            @RequestParam(defaultValue = "720") int lotCode
    ) {
        LotteryInfoResponse info = lotteryService.getCurrentInfo(lotCode);
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    /**
     * Get paginated lottery history list.
     *
     * @param lotCode  lottery code
     * @param pageNo   page number (1-based), default 1
     * @param pageSize items per page, default 30 (matches front-end history display)
     * @param date     optional filter date in YYYY-MM-DD
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<LotteryListResponse>> getList(
            @RequestParam(defaultValue = "720") int lotCode,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(required = false) String date
    ) {
        if (pageSize > 100) pageSize = 100;
        LotteryListResponse list = lotteryService.getHistoryList(lotCode, pageNo, pageSize, date);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Get the upstream lottery catalog for front-end dropdowns.
     */
    @GetMapping("/games")
    public ResponseEntity<ApiResponse<List<LotteryGameOption>>> getGames() {
        return ResponseEntity.ok(ApiResponse.success(lotteryService.getAvailableGames()));
    }
}
