package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.LotteryInfoResponse;
import com.bcbbs.backend.dto.LotteryIssueItem;
import com.bcbbs.backend.dto.LotteryListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    /**
     * Fetch current issue info (latest draw result + next draw countdown) from upstream.
     *
     * @param lotCode lottery code, e.g. 720 for 加拿大PC28
     * @return mapped LotteryInfoResponse
     */
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
     * Fetch paginated lottery history list from upstream.
     *
     * @param lotCode  lottery code
     * @param pageNo   page number (1-based)
     * @param pageSize items per page (default 30 for history display)
     * @return mapped LotteryListResponse
     */
    @SuppressWarnings("unchecked")
    public LotteryListResponse getHistoryList(int lotCode, int pageNo, int pageSize) {
        String url = UPSTREAM_BASE + "/api/lottery_code/getLotteryList?lotCode=" + lotCode
                + "&pageNo=" + pageNo + "&pageSize=" + pageSize
                + "&t=" + System.currentTimeMillis();
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

    private LotteryListResponse emptyList(int pageNo, int pageSize) {
        return LotteryListResponse.builder()
                .list(new ArrayList<>()).total(0).pageNo(pageNo).pageSize(pageSize).build();
    }
}
