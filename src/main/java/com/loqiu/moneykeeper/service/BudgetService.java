package com.loqiu.moneykeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.loqiu.moneykeeper.dto.BudgetRuleDTO;
import com.loqiu.moneykeeper.dto.LedgerBudgetDTO;
import com.loqiu.moneykeeper.entity.Budget;
import com.loqiu.moneykeeper.vo.BudgetRequest;
import com.loqiu.moneykeeper.vo.BudgetRuleRequest;

import java.util.List;

public interface BudgetService extends IService<Budget> {
    List<LedgerBudgetDTO> listBudgets(Long ledgerId, Integer year, Integer month, String type, Long categoryId);

    LedgerBudgetDTO getBudget(Long ledgerId, Long budgetId);

    LedgerBudgetDTO createBudget(Long ledgerId, Long currentUserId, BudgetRequest request);

    LedgerBudgetDTO updateBudget(Long ledgerId, Long budgetId, BudgetRequest request);

    void deleteBudget(Long ledgerId, Long budgetId);

    BudgetRuleDTO createRule(Long ledgerId, Long budgetId, BudgetRuleRequest request);

    BudgetRuleDTO updateRule(Long ledgerId, Long budgetId, Long ruleId, BudgetRuleRequest request);

    void deleteRule(Long ledgerId, Long budgetId, Long ruleId);
}