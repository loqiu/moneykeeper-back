package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.RecordSearchResultDTO;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.RecordSearchService;
import com.loqiu.moneykeeper.util.RecordTypeNormalizer;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.RecordSearchRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ledgers/{ledgerId}/search/records")
public class LedgerRecordSearchController {

    private static final Logger logger = LogManager.getLogger(LedgerRecordSearchController.class);
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @Autowired
    private RecordSearchService recordSearchService;

    @Autowired
    private LedgerService ledgerService;

    @GetMapping
    public ResponseEntity<List<RecordSearchResultDTO>> searchLedgerRecords(@PathVariable Long ledgerId,
                                                                           @ModelAttribute RecordSearchRequest searchRequest,
                                                                           HttpServletRequest request) {
        validateDateRange(searchRequest.getStartDate(), searchRequest.getEndDate());
        int limit = normalizeLimit(searchRequest.getLimit());
        requireLedgerViewer(request, ledgerId);

        logger.info("Searching ledger records - ledgerId: {}, requestedUserId: {}, currentUserId: {}, query: {}, type: {}, categoryId: {}",
                ledgerId,
                searchRequest.getUserId(),
                RequestAuthUtil.getCurrentUserId(request),
                searchRequest.getQuery(),
                searchRequest.getType(),
                searchRequest.getCategoryId());

        return ResponseEntity.ok(recordSearchService.searchLedgerRecords(
                ledgerId,
                searchRequest.getUserId(),
                searchRequest.getQuery(),
                RecordTypeNormalizer.normalizeOptional(searchRequest.getType(), "Record type must be income or expense"),
                searchRequest.getCategoryId(),
                searchRequest.getCategoryName(),
                searchRequest.getStartDate(),
                searchRequest.getEndDate(),
                limit
        ));
    }

    private void requireLedgerViewer(HttpServletRequest request, Long ledgerId) {
        ledgerService.requireLedger(ledgerId);
        if (RequestAuthUtil.isAdmin(request)) {
            return;
        }
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (!ledgerService.hasActiveMembership(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to search this ledger");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }
    }

    private int normalizeLimit(Integer requestedLimit) {
        int limit = requestedLimit == null ? DEFAULT_LIMIT : requestedLimit;
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }
        return limit;
    }
}
