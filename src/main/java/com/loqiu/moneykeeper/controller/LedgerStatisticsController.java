package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.LedgerStatisticsDTO;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.LedgerStatisticsService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ledgers/{ledgerId}/statistics")
public class LedgerStatisticsController {

    @Autowired
    private LedgerStatisticsService ledgerStatisticsService;

    @Autowired
    private LedgerService ledgerService;

    @GetMapping
    public ResponseEntity<LedgerStatisticsDTO> getStatistics(@PathVariable Long ledgerId,
                                                             @RequestParam(defaultValue = "month") String period,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchorDate,
                                                             @RequestParam(required = false) Long userId,
                                                             HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        validateTargetUser(ledgerId, userId);
        return ResponseEntity.ok(ledgerStatisticsService.getStatistics(ledgerId, period, anchorDate, userId));
    }

    private void requireLedgerViewer(HttpServletRequest request, Long ledgerId) {
        ledgerService.requireLedger(ledgerId);
        if (RequestAuthUtil.isAdmin(request)) {
            return;
        }
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (!ledgerService.hasActiveMembership(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to access this ledger");
        }
    }

    private void validateTargetUser(Long ledgerId, Long userId) {
        if (userId == null) {
            return;
        }
        if (!ledgerService.hasActiveMembership(ledgerId, userId)) {
            throw new BadRequestException("Target user is not an active member of this ledger");
        }
    }
}
