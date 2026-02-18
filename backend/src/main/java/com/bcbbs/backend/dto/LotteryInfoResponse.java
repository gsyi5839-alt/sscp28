package com.bcbbs.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Response DTO for current lottery issue info.
 * Maps to /api/lottery_code/getLotteryInfo response data field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotteryInfoResponse {

    /** Previous draw number codes, e.g. "1,0,8" */
    private String preDrawCode;

    /** Previous draw issue number */
    private String preDrawIssue;

    /** Previous draw time, e.g. "2026-02-18 03:15:30" */
    private String preDrawTime;

    /** Current/next draw issue number */
    private String drawIssue;

    /** Current/next draw time */
    private String drawTime;

    /** Lottery code, e.g. 720 */
    private Integer lotCode;

    /** Lottery name, e.g. "加拿大pc28" */
    private String lotName;

    /** Sum value of the previous draw (attr1) */
    private String sumValue;

    /** Big/Small classification (attr2) */
    private String sizeLabel;

    /** Odd/Even classification (attr3) */
    private String parityLabel;

    /** Combined size+parity label (attr4) */
    private String sizeParity;

    /** Pattern label - 豹子/顺子/对子/杂六/半顺 (attr5) */
    private String patternLabel;
}
