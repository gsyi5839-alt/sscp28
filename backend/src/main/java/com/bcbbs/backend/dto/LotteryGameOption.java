package com.bcbbs.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single upstream lottery option exposed to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotteryGameOption {

    /** Lottery code, e.g. 720 */
    private Integer lotCode;

    /** Lottery name, e.g. "加拿大pc28" */
    private String lotName;

    /** Upstream lottery type category */
    private Integer lotType;

    /** Upstream recommendation label */
    private Integer lotLabel;

    /** Upstream sort order */
    private Integer sort;
}
