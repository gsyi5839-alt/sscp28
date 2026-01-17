package com.bcbbs.backend.repository;

import com.bcbbs.backend.entity.SearchItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchItemRepository extends JpaRepository<SearchItem, Long> {

    List<SearchItem> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titleKeyword,
            String descriptionKeyword
    );
}

