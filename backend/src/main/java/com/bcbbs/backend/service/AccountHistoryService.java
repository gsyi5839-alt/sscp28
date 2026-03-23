package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.AccountHistoryRequest;
import com.bcbbs.backend.dto.AccountHistoryResponse;
import com.bcbbs.backend.dto.SettlementDetailResponse;
import com.bcbbs.backend.dto.SettlementReportRequest;
import com.bcbbs.backend.entity.AccountHistory;
import com.bcbbs.backend.entity.SettlementDetail;
import com.bcbbs.backend.repository.AccountHistoryRepository;
import com.bcbbs.backend.repository.SettlementDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing account history and settlement data.
 * Handles daily aggregated statistics and detailed settlement reports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountHistoryService {

    private final AccountHistoryRepository accountHistoryRepository;
    private final SettlementDetailRepository settlementDetailRepository;

    /**
     * Weekday names in Chinese for display purposes.
     */
    private static final String[] WEEKDAY_NAMES = {
            "周日", "周一", "周二", "周三", "周四", "周五", "周六"
    };

    /**
     * Get account history for the last two weeks (last week + this week).
     * Returns aggregated daily statistics for each day in the two-week period.
     *
     * @param userId the user ID
     * @return list of account history responses for the last two weeks
     */
    @Transactional(readOnly = true)
    public List<AccountHistoryResponse> getLastTwoWeeksHistory(Long userId) {
        log.debug("Fetching last two weeks account history for user: {}", userId);

        List<LocalDate> twoWeekDates = calculateLastTwoWeeksDates();
        List<AccountHistory> histories = accountHistoryRepository.findByUserIdAndTransactionDates(userId, twoWeekDates);

        return convertToAccountHistoryResponse(histories, twoWeekDates);
    }

    /**
     * Get account history for a specific date range.
     *
     * @param userId the user ID
     * @param request the date range request
     * @return list of account history responses
     */
    @Transactional(readOnly = true)
    public List<AccountHistoryResponse> getAccountHistoryByDateRange(Long userId, AccountHistoryRequest request) {
        log.debug("Fetching account history for user: {} from {} to {}",
                userId, request.getStartDate(), request.getEndDate());

        List<AccountHistory> histories;

        if (request.getDates() != null && !request.getDates().isEmpty()) {
            histories = accountHistoryRepository.findByUserIdAndTransactionDates(userId, request.getDates());
        } else if (request.getStartDate() != null && request.getEndDate() != null) {
            histories = accountHistoryRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    userId, request.getStartDate(), request.getEndDate());
        } else {
            return getLastTwoWeeksHistory(userId);
        }

        return convertToAccountHistoryResponse(histories, null);
    }

    /**
     * Get settlement report details for a specific date.
     * Returns detailed breakdown by game type for the given date.
     *
     * @param userId the user ID
     * @param request the settlement report request containing the date
     * @return list of settlement detail responses
     */
    @Transactional(readOnly = true)
    public List<SettlementDetailResponse> getSettlementReport(Long userId, SettlementReportRequest request) {
        if (request.getDate() == null) {
            throw new IllegalArgumentException("Date is required for settlement report");
        }

        log.debug("Fetching settlement report for user: {} on date: {}", userId, request.getDate());

        List<SettlementDetail> details = settlementDetailRepository
                .findByUserIdAndTransactionDateOrderByGameTypeAsc(userId, request.getDate());

        return convertToSettlementDetailResponse(details);
    }

    /**
     * Calculate dates for the last two weeks (last week Monday to this week Sunday).
     *
     * @return list of 14 dates representing the last two weeks
     */
    private List<LocalDate> calculateLastTwoWeeksDates() {
        LocalDate today = LocalDate.now();
        DayOfWeek currentDayOfWeek = today.getDayOfWeek();

        int daysSinceMonday = currentDayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();
        if (daysSinceMonday < 0) {
            daysSinceMonday += 7;
        }

        LocalDate thisWeekMonday = today.minusDays(daysSinceMonday);
        LocalDate lastWeekMonday = thisWeekMonday.minusWeeks(1);

        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            dates.add(lastWeekMonday.plusDays(i));
        }

        return dates;
    }

    /**
     * Convert AccountHistory entities to response DTOs.
     * If a date has no record, creates a zero-value response for that date.
     *
     * @param histories the account history entities
     * @param expectedDates the expected dates (for filling in missing dates)
     * @return list of account history responses
     */
    private List<AccountHistoryResponse> convertToAccountHistoryResponse(
            List<AccountHistory> histories, List<LocalDate> expectedDates) {

        List<AccountHistoryResponse> responses = new ArrayList<>();

        if (expectedDates != null) {
            for (LocalDate date : expectedDates) {
                AccountHistory history = histories.stream()
                        .filter(h -> h.getTransactionDate().equals(date))
                        .findFirst()
                        .orElse(null);

                responses.add(buildAccountHistoryResponse(date, history));
            }
        } else {
            for (AccountHistory history : histories) {
                responses.add(buildAccountHistoryResponse(history.getTransactionDate(), history));
            }
        }

        return responses;
    }

    /**
     * Build an AccountHistoryResponse from entity or create a zero-value response.
     *
     * @param date the transaction date
     * @param history the account history entity (may be null)
     * @return the account history response
     */
    private AccountHistoryResponse buildAccountHistoryResponse(LocalDate date, AccountHistory history) {
        return AccountHistoryResponse.builder()
                .date(date)
                .weekday(getWeekdayName(date))
                .orderCount(history != null ? history.getOrderCount() : 0)
                .betAmount(history != null ? history.getBetAmount() : BigDecimal.ZERO)
                .validAmount(history != null ? history.getValidAmount() : BigDecimal.ZERO)
                .rebate(history != null ? history.getRebate() : BigDecimal.ZERO)
                .winLoss(history != null ? history.getWinLoss() : BigDecimal.ZERO)
                .settlementStatus(history != null ? history.getSettlementStatus().name() : "PENDING")
                .build();
    }

    /**
     * Convert SettlementDetail entities to response DTOs.
     *
     * @param details the settlement detail entities
     * @return list of settlement detail responses
     */
    private List<SettlementDetailResponse> convertToSettlementDetailResponse(List<SettlementDetail> details) {
        List<SettlementDetailResponse> responses = new ArrayList<>();

        for (SettlementDetail detail : details) {
            responses.add(SettlementDetailResponse.builder()
                    .date(detail.getTransactionDate())
                    .gameType(detail.getGameType())
                    .gameName(detail.getGameName())
                    .orderCount(detail.getOrderCount())
                    .betAmount(detail.getBetAmount())
                    .validAmount(detail.getValidAmount())
                    .rebate(detail.getRebate())
                    .winLoss(detail.getWinLoss())
                    .remark(detail.getRemark())
                    .build());
        }

        return responses;
    }

    /**
     * Get the Chinese weekday name for a date.
     *
     * @param date the date
     * @return the weekday name in Chinese
     */
    private String getWeekdayName(LocalDate date) {
        return WEEKDAY_NAMES[date.getDayOfWeek().getValue() % 7];
    }
}
