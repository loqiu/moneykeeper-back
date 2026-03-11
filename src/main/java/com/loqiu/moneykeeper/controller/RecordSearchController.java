package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.RecordSearchIndexStatsDTO;
import com.loqiu.moneykeeper.dto.RecordSearchReindexResultDTO;
import com.loqiu.moneykeeper.dto.RecordSearchResultDTO;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.service.RecordSearchService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search/records")
public class RecordSearchController {

    private static final Logger logger = LogManager.getLogger(RecordSearchController.class);
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @Autowired
    private RecordSearchService recordSearchService;

    @GetMapping
    public ResponseEntity<List<RecordSearchResultDTO>> searchRecords(@ModelAttribute RecordSearchRequest searchRequest,
                                                                     HttpServletRequest request) {
        validateDateRange(searchRequest.getStartDate(), searchRequest.getEndDate());
        int limit = normalizeLimit(searchRequest.getLimit());
        Long targetUserId = resolveTargetUserId(request, searchRequest.getUserId());

        logger.info("Searching records - targetUserId: {}, currentUserId: {}, query: {}, type: {}, categoryId: {}",
                targetUserId,
                RequestAuthUtil.getCurrentUserId(request),
                searchRequest.getQuery(),
                searchRequest.getType(),
                searchRequest.getCategoryId());

        return ResponseEntity.ok(recordSearchService.searchRecords(
                targetUserId,
                searchRequest.getQuery(),
                searchRequest.getType(),
                searchRequest.getCategoryId(),
                searchRequest.getCategoryName(),
                searchRequest.getStartDate(),
                searchRequest.getEndDate(),
                limit
        ));
    }

    @PostMapping("/reindex")
    public ResponseEntity<RecordSearchReindexResultDTO> reindexRecords(@RequestParam(required = false) Long userId,
                                                                       HttpServletRequest request) {
        requireAdmin(request);
        logger.info("Reindexing record search index - scope: {}, requestedBy: {}",
                userId == null ? "all" : "user=" + userId,
                RequestAuthUtil.getCurrentUserId(request));
        return ResponseEntity.ok(recordSearchService.reindexRecords(userId));
    }

    @PostMapping("/reindex/ledger")
    public ResponseEntity<RecordSearchReindexResultDTO> reindexLedgerRecords(@RequestParam Long ledgerId,
                                                                             HttpServletRequest request) {
        requireAdmin(request);
        if (ledgerId == null) {
            throw new BadRequestException("Ledger id is required");
        }
        logger.info("Reindexing record search index for ledger - ledgerId: {}, requestedBy: {}",
                ledgerId,
                RequestAuthUtil.getCurrentUserId(request));
        return ResponseEntity.ok(recordSearchService.reindexLedgerRecords(ledgerId));
    }

    @GetMapping("/stats")
    public ResponseEntity<RecordSearchIndexStatsDTO> getIndexStats(@RequestParam(required = false) Long userId,
                                                                   HttpServletRequest request) {
        requireAdmin(request);
        logger.info("Getting record search index stats - scope: {}, requestedBy: {}",
                userId == null ? "all" : "user=" + userId,
                RequestAuthUtil.getCurrentUserId(request));
        return ResponseEntity.ok(recordSearchService.getIndexStats(userId));
    }

    @GetMapping("/stats/ledger")
    public ResponseEntity<RecordSearchIndexStatsDTO> getLedgerIndexStats(@RequestParam Long ledgerId,
                                                                         HttpServletRequest request) {
        requireAdmin(request);
        if (ledgerId == null) {
            throw new BadRequestException("Ledger id is required");
        }
        logger.info("Getting record search index stats for ledger - ledgerId: {}, requestedBy: {}",
                ledgerId,
                RequestAuthUtil.getCurrentUserId(request));
        return ResponseEntity.ok(recordSearchService.getLedgerIndexStats(ledgerId));
    }

    private Long resolveTargetUserId(HttpServletRequest request, Long requestedUserId) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (requestedUserId == null) {
            return currentUserId;
        }
        if (!RequestAuthUtil.isSelfOrAdmin(request, requestedUserId)) {
            throw new ForbiddenException("You do not have permission to search this user's records");
        }
        return requestedUserId;
    }

    private void requireAdmin(HttpServletRequest request) {
        if (!RequestAuthUtil.isAdmin(request)) {
            throw new ForbiddenException("Admin role is required");
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