package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.LotteryInfoResponse;
import com.bcbbs.backend.dto.LotteryGameOption;
import com.bcbbs.backend.dto.LotteryIssueItem;
import com.bcbbs.backend.dto.LotteryListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service to proxy lottery data from the upstream API at bw1284.cc.
 * Endpoints used:
 *   GET /api/lottery_code/getLotteryInfo?lotCode={code}
 *   GET /api/lottery_code/getLotteryList?lotCode={code}&pageNo={n}&pageSize={size}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryService {

    /** Upstream lottery API base URL */
    private static final String UPSTREAM_BASE = "https://bw1284.cc";

    private final RestTemplate restTemplate;
    
    /** Self-reference for invoking cached methods through Spring proxy */
    @Lazy
    @Autowired
    private LotteryService self;

    /**
     * Fetch current issue info (latest draw result + next draw countdown) from upstream.
     *
     * @param lotCode lottery code, e.g. 720 for 加拿大PC28
     * @return mapped LotteryInfoResponse
     */
    @Cacheable(value = "lotteryInfo", key = "#lotCode")
    @SuppressWarnings("unchecked")
    public LotteryInfoResponse getCurrentInfo(int lotCode) {
        String url = UPSTREAM_BASE + "/api/lottery_code/getLotteryInfo?lotCode=" + lotCode
                + "&t=" + System.currentTimeMillis();
        try {
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null || !Integer.valueOf(0).equals(resp.get("code"))) {
                log.warn("Upstream getLotteryInfo returned unexpected response for lotCode={}", lotCode);
                return new LotteryInfoResponse();
            }
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null) return new LotteryInfoResponse();

            return LotteryInfoResponse.builder()
                    .preDrawCode(str(data, "preDrawCode"))
                    .preDrawIssue(str(data, "preDrawIssue"))
                    .preDrawTime(str(data, "preDrawTime"))
                    .drawIssue(str(data, "drawIssue"))
                    .drawTime(str(data, "drawTime"))
                    .lotCode(toInt(data, "lotCode"))
                    .lotName(str(data, "lotName"))
                    .sumValue(str(data, "attr1"))
                    .sizeLabel(str(data, "attr2"))
                    .parityLabel(str(data, "attr3"))
                    .sizeParity(str(data, "attr4"))
                    .patternLabel(str(data, "attr5"))
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch lottery info for lotCode={}: {}", lotCode, e.getMessage());
            return new LotteryInfoResponse();
        }
    }

    /**
     * Fetch paginated lottery history list with smart caching strategy.
     * Page 1: cached 5 seconds (frequent updates)
     * Other pages: cached 15 minutes (historical data, stable)
     *
     * @param lotCode  lottery code
     * @param pageNo   page number (1-based)
     * @param pageSize items per page (default 30 for history display)
     * @param date     optional filter date in YYYY-MM-DD
     * @return mapped LotteryListResponse
     */
    public LotteryListResponse getHistoryList(int lotCode, int pageNo, int pageSize, String date) {
        LotteryService invoker = getCacheInvoker();
        if (pageNo <= 1) {
            return invoker.getHistoryListFirstPage(lotCode, pageSize, date);
        }
        return invoker.getHistoryListOtherPages(lotCode, pageNo, pageSize, date);
    }

    /**
     * Return Spring proxy when available; otherwise fall back to current instance.
     * This keeps unit tests (without Spring context) from failing with null self.
     */
    private LotteryService getCacheInvoker() {
        return self != null ? self : this;
    }

    /**
     * First page cache: 5 second TTL for latest draw results.
     */
    @Cacheable(value = "lotteryListFirstPage",
        key = "#lotCode + ':' + #pageSize + ':' + (#date != null ? #date : 'all')")
    public LotteryListResponse getHistoryListFirstPage(int lotCode, int pageSize, String date) {
        return fetchHistoryFromUpstream(lotCode, 1, pageSize, date);
    }

    /**
     * Other pages cache: 15 minute TTL for historical data.
     */
    @Cacheable(value = "lotteryListOtherPages",
        key = "#lotCode + ':' + #pageNo + ':' + #pageSize + ':' + (#date != null ? #date : 'all')")
    public LotteryListResponse getHistoryListOtherPages(int lotCode, int pageNo, int pageSize, String date) {
        return fetchHistoryFromUpstream(lotCode, pageNo, pageSize, date);
    }

    /**
     * Fetch history list from upstream API.
     */
    @SuppressWarnings("unchecked")
    private LotteryListResponse fetchHistoryFromUpstream(int lotCode, int pageNo, int pageSize, String date) {
        String url = UriComponentsBuilder
                .fromHttpUrl(UPSTREAM_BASE + "/api/lottery_code/getLotteryList")
                .queryParam("lotCode", lotCode)
                .queryParam("pageNo", pageNo)
                .queryParam("pageSize", pageSize)
                .queryParam("t", System.currentTimeMillis())
                .queryParamIfPresent("date", isBlank(date) ? java.util.Optional.empty() : java.util.Optional.of(date))
                .toUriString();
        try {
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null || !Integer.valueOf(0).equals(resp.get("code"))) {
                log.warn("Upstream getLotteryList returned unexpected response for lotCode={}", lotCode);
                return emptyList(pageNo, pageSize);
            }
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null) return emptyList(pageNo, pageSize);

            List<Map<String, Object>> rawList = (List<Map<String, Object>>) data.get("list");
            List<LotteryIssueItem> items = new ArrayList<>();
            if (rawList != null) {
                for (Map<String, Object> item : rawList) {
                    items.add(LotteryIssueItem.builder()
                            .preDrawIssue(str(item, "preDrawIssue"))
                            .preDrawCode(str(item, "preDrawCode"))
                            .preDrawTime(str(item, "preDrawTime"))
                            .sumValue(str(item, "attr1"))
                            .sizeLabel(str(item, "attr2"))
                            .parityLabel(str(item, "attr3"))
                            .sizeParity(str(item, "attr4"))
                            .patternLabel(str(item, "attr5"))
                            .build());
                }
            }

            Object totalObj = data.get("total");
            long total = totalObj instanceof Number ? ((Number) totalObj).longValue() : items.size();

            return LotteryListResponse.builder()
                    .list(items)
                    .total(total)
                    .pageNo(pageNo)
                    .pageSize(pageSize)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch lottery list for lotCode={}: {}", lotCode, e.getMessage());
            return emptyList(pageNo, pageSize);
        }
    }

    /**
     * Fetch available lotteries from the upstream allLottery endpoint.
     */
    @Cacheable(value = "lotteryGames", unless = "#result == null || #result.isEmpty()")
    @SuppressWarnings("unchecked")
    public List<LotteryGameOption> getAvailableGames() {
        String url = UPSTREAM_BASE + "/api/lottery_code/allLottery?t=" + System.currentTimeMillis();
        try {
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null || !Integer.valueOf(0).equals(resp.get("code"))) {
                log.warn("Upstream allLottery returned unexpected response");
                return List.of();
            }

            List<Map<String, Object>> rawList = (List<Map<String, Object>>) resp.get("data");
            if (rawList == null || rawList.isEmpty()) {
                return List.of();
            }

            List<LotteryGameOption> games = new ArrayList<>(rawList.size());
            for (Map<String, Object> item : rawList) {
                games.add(LotteryGameOption.builder()
                        .lotCode(toInt(item, "lotCode"))
                        .lotName(str(item, "lotName"))
                        .lotType(toInt(item, "lotType"))
                        .lotLabel(toInt(item, "lotLabel"))
                        .sort(toInt(item, "sort"))
                        .build());
            }
            return games;
        } catch (Exception e) {
            log.error("Failed to fetch lottery games: {}", e.getMessage());
            return List.of();
        }
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private Integer toInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private LotteryListResponse emptyList(int pageNo, int pageSize) {
        return LotteryListResponse.builder()
                .list(new ArrayList<>()).total(0).pageNo(pageNo).pageSize(pageSize).build();
    }
}
