package com.bcbbs.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * Response DTO for lottery history list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotteryListResponse {

    /** History records */
    private List<LotteryIssueItem> list;

    /** Total count */
    private long total;

    /** Current page number */
    private int pageNo;

    /** Page size */
    private int pageSize;
}
