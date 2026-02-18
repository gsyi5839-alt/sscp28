package com.bcbbs.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Single lottery issue record for history list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotteryIssueItem {

    /** Issue number */
    private String preDrawIssue;

    /** Draw result codes, e.g. "1,0,8" */
    private String preDrawCode;

    /** Draw time */
    private String preDrawTime;

    /** Sum value (attr1) */
    private String sumValue;

    /** Big/Small (attr2) */
    private String sizeLabel;

    /** Odd/Even (attr3) */
    private String parityLabel;

    /** Combined size+parity (attr4) */
    private String sizeParity;

    /** Pattern label (attr5) */
    private String patternLabel;
}
