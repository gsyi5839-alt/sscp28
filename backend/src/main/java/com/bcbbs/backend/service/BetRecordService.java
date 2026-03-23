package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.BetPageResponse;
import com.bcbbs.backend.dto.BetRecordResponse;
import com.bcbbs.backend.entity.BetRecord;
import com.bcbbs.backend.repository.BetRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing bet records.
 * Provides paginated queries for unsettled bets.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BetRecordService {

    private final BetRecordRepository betRecordRepository;

    /**
     * Get paginated unsettled bet records for a user.
     *
     * @param userId the user ID
     * @param page page number (1-based)
     * @param size page size
     * @return paginated bet record response
     */
    @Transactional(readOnly = true)
    public BetPageResponse getUnsettledBets(Long userId, int page, int size) {
        log.debug("Fetching unsettled bets for user: {}, page: {}, size: {}", userId, page, size);

        // Convert 1-based page to 0-based for Spring Data
        Page<BetRecord> betPage = betRecordRepository.findByUserIdAndSettledOrderByBetTimeDesc(
                userId, false, PageRequest.of(page - 1, size));

        List<BetRecordResponse> records = betPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return BetPageResponse.builder()
                .list(records)
                .page(page)
                .size(size)
                .total(betPage.getTotalElements())
                .build();
    }

    /**
     * Convert BetRecord entity to BetRecordResponse DTO.
     *
     * @param record the bet record entity
     * @return the bet record response DTO
     */
    private BetRecordResponse toResponse(BetRecord record) {
        return BetRecordResponse.builder()
                .orderNo(record.getOrderNo())
                .time(record.getBetTime())
                .type(record.getGameType())
                .playMethod(record.getPlayMethod())
                .handicap(record.getHandicap())
                .betAmount(record.getBetAmount())
                .rebate(record.getRebate())
                .canWin(record.getCanWin())
                .build();
    }
}
