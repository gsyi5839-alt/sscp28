package com.bcbbs.backend.repository;

import com.bcbbs.backend.entity.AccessLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLineRepository extends JpaRepository<AccessLine, Long> {

    List<AccessLine> findByTypeAndActiveTrueOrderBySortOrderAsc(AccessLine.LineType type);
}

