package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.SearchItemResponse;
import com.bcbbs.backend.repository.SearchItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchItemRepository searchItemRepository;

    /**
     * Search items by keyword in title and description.
     */
    public List<SearchItemResponse> search(String keyword) {
        return searchItemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(item -> SearchItemResponse.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .url(item.getUrl())
                        .build())
                .toList();
    }
}

