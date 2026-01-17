package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.LineResponse;
import com.bcbbs.backend.entity.AccessLine;
import com.bcbbs.backend.repository.AccessLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessLineService {

    private final AccessLineRepository accessLineRepository;

    /**
     * Load active lines by type in display order.
     */
    public List<LineResponse> getActiveLines(AccessLine.LineType type) {
        return accessLineRepository.findByTypeAndActiveTrueOrderBySortOrderAsc(type)
                .stream()
                .map(line -> LineResponse.builder()
                        .id(line.getId())
                        .name(line.getName())
                        .url(line.getUrl())
                        .type(line.getType().name())
                        .pingMs(line.getLastPingMs())
                        .build())
                .toList();
    }
}

