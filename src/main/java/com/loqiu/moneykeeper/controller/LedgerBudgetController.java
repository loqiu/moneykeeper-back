package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.BudgetRuleDTO;
import com.loqiu.moneykeeper.dto.LedgerBudgetDTO;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.service.BudgetService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.BudgetRequest;
import com.loqiu.moneykeeper.vo.BudgetRuleRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledgers/{ledgerId}/budgets")
public class LedgerBudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private LedgerService ledgerService;

    @GetMapping
    public ResponseEntity<List<LedgerBudgetDTO>> listBudgets(@PathVariable Long ledgerId,
                                                             @RequestParam(required = false) Integer year,
                                                             @RequestParam(required = false) Integer month,
                                                             @RequestParam(required = false) String type,
                                                             @RequestParam(required = false) Long categoryId,
                                                             HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        return ResponseEntity.ok(budgetService.listBudgets(ledgerId, year, month, type, categoryId));
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<LedgerBudgetDTO> getBudget(@PathVariable Long ledgerId,
                                                     @PathVariable Long budgetId,
                                                     HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        return ResponseEntity.ok(budgetService.getBudget(ledgerId, budgetId));
    }

    @PostMapping
    public ResponseEntity<LedgerBudgetDTO> createBudget(@PathVariable Long ledgerId,
                                                        @RequestBody(required = false) BudgetRequest requestBody,
                                                        HttpServletRequest request) {
        requireLedgerManager(request, ledgerId);
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return ResponseEntity.ok(budgetService.createBudget(ledgerId, currentUserId, requestBody));
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<LedgerBudgetDTO> updateBudget(@PathVariable Long ledgerId,
                                                        @PathVariable Long budgetId,
                                                        @RequestBody(required = false) BudgetRequest requestBody,
                                                        HttpServletRequest request) {
        requireLedgerManager(request, ledgerId);
        return ResponseEntity.ok(budgetService.updateBudget(ledgerId, budgetId, requestBody));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long ledgerId,
                                             @PathVariable Long budgetId,
                                             HttpServletRequest request) {
        requireLedgerManager(request, ledgerId);
        budgetService.deleteBudget(ledgerId, budgetId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{budgetId}/rules")
    public ResponseEntity<BudgetRuleDTO> createRule(@PathVariable Long ledgerId,
                                                    @PathVariable Long budgetId,
                                                    @RequestBody(required = false) BudgetRuleRequest requestBody,
                                                    HttpServletRequest request) {
        requireLedgerManager(request, ledgerId);
        return ResponseEntity.ok(budgetService.createRule(ledgerId, budgetId, requestBody));
    }

    @PutMapping("/{budgetId}/rules/{ruleId}")
    public ResponseEntity<BudgetRuleDTO> updateRule(@PathVariable Long ledgerId,
                                                    @PathVariable Long budgetId,
                                                    @PathVariable Long ruleId,
                                                    @RequestBody(required = false) BudgetRuleRequest requestBody,
                                                    HttpServletRequest request) {
        requireLedgerManager(request, ledgerId);
        return ResponseEntity.ok(budgetService.updateRule(ledgerId, budgetId, ruleId, requestBody));
    }

    @DeleteMapping("/{budgetId}/rules/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long ledgerId,
                                           @PathVariable Long budgetId,
                                           @PathVariable Long ruleId,
                                           HttpServletRequest request) {
        requireLedgerManager(request, ledgerId);
        budgetService.deleteRule(ledgerId, budgetId, ruleId);
        return ResponseEntity.ok().build();
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

    private void requireLedgerManager(HttpServletRequest request, Long ledgerId) {
        ledgerService.requireLedger(ledgerId);
        if (RequestAuthUtil.isAdmin(request)) {
            return;
        }
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (!ledgerService.hasManagementPermission(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to manage this ledger's budgets");
        }
    }
}