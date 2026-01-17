package com.bcbbs.backend.controller;

import com.bcbbs.backend.dto.ApiResponse;
import com.bcbbs.backend.dto.CaptchaResponse;
import com.bcbbs.backend.dto.LineResponse;
import com.bcbbs.backend.dto.SearchItemResponse;
import com.bcbbs.backend.entity.AccessLine;
import com.bcbbs.backend.service.AccessLineService;
import com.bcbbs.backend.service.CaptchaService;
import com.bcbbs.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final AccessLineService accessLineService;
    private final SearchService searchService;
    private final CaptchaService captchaService;

    /**
     * Search items by keyword.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchItemResponse>>> search(
            @RequestParam("q") String keyword
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Search keyword is required"));
        }
        List<SearchItemResponse> results = searchService.search(keyword.trim());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Load active member or agent lines.
     */
    @GetMapping("/lines")
    public ResponseEntity<ApiResponse<List<LineResponse>>> getLines(
            @RequestParam("type") String type
    ) {
        AccessLine.LineType lineType;
        try {
            lineType = AccessLine.LineType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid line type"));
        }
        return ResponseEntity.ok(ApiResponse.success(accessLineService.getActiveLines(lineType)));
    }

    /**
     * Generate a new captcha token for login.
     */
    @GetMapping("/captcha")
    public ResponseEntity<ApiResponse<CaptchaResponse>> captcha() {
        return ResponseEntity.ok(ApiResponse.success(captchaService.createCaptcha()));
    }
}

