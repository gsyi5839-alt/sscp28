package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineResponse {

    private Long id;
    private String name;
    private String url;
    private String type;
    private Integer pingMs;
}

