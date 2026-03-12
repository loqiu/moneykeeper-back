package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loqiu.moneykeeper.dto.BudgetProgressDTO;
import com.loqiu.moneykeeper.dto.BudgetRuleDTO;
import com.loqiu.moneykeeper.dto.LedgerBudgetDTO;
import com.loqiu.moneykeeper.entity.Budget;
import com.loqiu.moneykeeper.entity.BudgetRule;
import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ConflictException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.mapper.BudgetMapper;
import com.loqiu.moneykeeper.mapper.BudgetRuleMapper;
import com.loqiu.moneykeeper.service.BudgetService;
import com.loqiu.moneykeeper.service.CategoryService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.vo.BudgetRequest;
import com.loqiu.moneykeeper.vo.BudgetRuleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl extends ServiceImpl<BudgetMapper, Budget> implements BudgetService {

    private static final String PERIOD_TYPE_MONTHLY = "monthly";
    private static final String RULE_TYPE_THRESHOLD = "threshold";

    @Autowired
    private BudgetRuleMapper budgetRuleMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @Override
    public List<LedgerBudgetDTO> listBudgets(Long ledgerId, Integer year, Integer month, String type, Long categoryId) {
        QueryWrapper<Budget> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ledger_id", ledgerId)
                .orderByDesc("budget_year", "budget_month", "created_at", "id");
        if (year != null) {
            queryWrapper.eq("budget_year", year);
        }
        if (month != null) {
            queryWrapper.eq("budget_month", month);
        }
        if (StringUtils.hasText(type)) {
            queryWrapper.eq("type", normalizeType(type));
        }
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }
        return enrichBudgets(list(queryWrapper));
    }

    @Override
    public LedgerBudgetDTO getBudget(Long ledgerId, Long budgetId) {
        Budget budget = requireBudget(ledgerId, budgetId);
        return toDto(budget, loadRuleDtos(List.of(budgetId)), loadCategories(List.of(budget)));
    }

    @Override
    @Transactional
    public LedgerBudgetDTO createBudget(Long ledgerId, Long currentUserId, BudgetRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        String normalizedName = normalizeRequiredText(request.getName(), "Budget name is required");
        String normalizedType = normalizeType(request.getType());
        BigDecimal validAmount = validateAmount(request.getAmount());
        int validYear = validateYear(request.getBudgetYear());
        int validMonth = validateMonth(request.getBudgetMonth());
        Category category = validateCategory(ledgerId, request.getCategoryId(), normalizedType);
        ensureNoDuplicateBudget(ledgerId, null, validYear, validMonth, normalizedType, category == null ? null : category.getId());

        YearMonth yearMonth = YearMonth.of(validYear, validMonth);
        Budget budget = Budget.builder()
                .ledgerId(ledgerId)
                .createdByUserId(currentUserId)
                .categoryId(category == null ? null : category.getId())
                .name(normalizedName)
                .periodType(PERIOD_TYPE_MONTHLY)
                .budgetYear(validYear)
                .budgetMonth(validMonth)
                .startDate(yearMonth.atDay(1))
                .endDate(yearMonth.atEndOfMonth())
                .type(normalizedType)
                .amount(validAmount)
                .notes(trimToNull(request.getNotes()))
                .build();
        save(budget);
        return toDto(budget, Map.of(budget.getId(), List.of()), loadCategories(List.of(budget)));
    }

    @Override
    @Transactional
    public LedgerBudgetDTO updateBudget(Long ledgerId, Long budgetId, BudgetRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        Budget existingBudget = requireBudget(ledgerId, budgetId);
        validateUpdateRequest(request);

        String normalizedName = request.getName() == null ? existingBudget.getName() : normalizeRequiredText(request.getName(), "Budget name cannot be blank");
        String normalizedType = request.getType() == null ? existingBudget.getType() : normalizeType(request.getType());
        BigDecimal amount = request.getAmount() == null ? existingBudget.getAmount() : validateAmount(request.getAmount());
        int year = request.getBudgetYear() == null ? existingBudget.getBudgetYear() : validateYear(request.getBudgetYear());
        int month = request.getBudgetMonth() == null ? existingBudget.getBudgetMonth() : validateMonth(request.getBudgetMonth());
        Long categoryId = request.getCategoryId() == null ? existingBudget.getCategoryId() : request.getCategoryId();
        Category category = validateCategory(ledgerId, categoryId, normalizedType);
        ensureNoDuplicateBudget(ledgerId, budgetId, year, month, normalizedType, category == null ? null : category.getId());

        YearMonth yearMonth = YearMonth.of(year, month);
        existingBudget.setName(normalizedName);
        existingBudget.setType(normalizedType);
        existingBudget.setAmount(amount);
        existingBudget.setBudgetYear(year);
        existingBudget.setBudgetMonth(month);
        existingBudget.setStartDate(yearMonth.atDay(1));
        existingBudget.setEndDate(yearMonth.atEndOfMonth());
        existingBudget.setCategoryId(category == null ? null : category.getId());
        if (request.getNotes() != null) {
            existingBudget.setNotes(trimToNull(request.getNotes()));
        }
        updateById(existingBudget);
        return toDto(existingBudget, loadRuleDtos(List.of(existingBudget.getId())), loadCategories(List.of(existingBudget)));
    }

    @Override
    @Transactional
    public void deleteBudget(Long ledgerId, Long budgetId) {
        Budget existingBudget = requireBudget(ledgerId, budgetId);
        existingBudget.setDeletedAt(1);
        existingBudget.setDeletedTime(LocalDateTime.now());
        updateById(existingBudget);
    }

    @Override
    @Transactional
    public BudgetRuleDTO createRule(Long ledgerId, Long budgetId, BudgetRuleRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        requireBudget(ledgerId, budgetId);

        BudgetRule rule = BudgetRule.builder()
                .budgetId(budgetId)
                .ruleType(RULE_TYPE_THRESHOLD)
                .thresholdPercentage(validateThreshold(request.getThresholdPercentage()))
                .enabled(resolveEnabled(request.getEnabled()))
                .notificationTitle(trimToNull(request.getNotificationTitle()))
                .notificationMessage(trimToNull(request.getNotificationMessage()))
                .build();
        budgetRuleMapper.insert(rule);
        return toRuleDto(rule);
    }

    @Override
    @Transactional
    public BudgetRuleDTO updateRule(Long ledgerId, Long budgetId, Long ruleId, BudgetRuleRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getThresholdPercentage() == null
                && request.getEnabled() == null
                && request.getNotificationTitle() == null
                && request.getNotificationMessage() == null) {
            throw new BadRequestException("At least one budget rule field must be provided");
        }

        requireBudget(ledgerId, budgetId);
        BudgetRule rule = requireRule(budgetId, ruleId);
        if (request.getThresholdPercentage() != null) {
            rule.setThresholdPercentage(validateThreshold(request.getThresholdPercentage()));
        }
        if (request.getEnabled() != null) {
            rule.setEnabled(resolveEnabled(request.getEnabled()));
        }
        if (request.getNotificationTitle() != null) {
            rule.setNotificationTitle(trimToNull(request.getNotificationTitle()));
        }
        if (request.getNotificationMessage() != null) {
            rule.setNotificationMessage(trimToNull(request.getNotificationMessage()));
        }
        budgetRuleMapper.updateById(rule);
        return toRuleDto(rule);
    }

    @Override
    @Transactional
    public void deleteRule(Long ledgerId, Long budgetId, Long ruleId) {
        requireBudget(ledgerId, budgetId);
        BudgetRule rule = requireRule(budgetId, ruleId);
        budgetRuleMapper.deleteById(rule.getId());
    }

    private List<LedgerBudgetDTO> enrichBudgets(List<Budget> budgets) {
        Map<Long, List<BudgetRuleDTO>> rulesByBudgetId = loadRuleDtos(
                budgets.stream().map(Budget::getId).filter(Objects::nonNull).toList()
        );
        Map<Long, Category> categoriesById = loadCategories(budgets);
        return budgets.stream()
                .map(budget -> toDto(budget, rulesByBudgetId, categoriesById))
                .toList();
    }

    private Map<Long, List<BudgetRuleDTO>> loadRuleDtos(List<Long> budgetIds) {
        if (budgetIds == null || budgetIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<BudgetRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("budget_id", budgetIds)
                .orderByAsc("threshold_percentage", "id");
        return budgetRuleMapper.selectList(queryWrapper).stream()
                .map(this::toRuleDto)
                .collect(Collectors.groupingBy(BudgetRuleDTO::getBudgetId, Collectors.toList()));
    }

    private Map<Long, Category> loadCategories(List<Budget> budgets) {
        List<Long> categoryIds = budgets.stream()
                .map(Budget::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity(), (left, right) -> left));
    }

    private LedgerBudgetDTO toDto(Budget budget,
                                  Map<Long, List<BudgetRuleDTO>> rulesByBudgetId,
                                  Map<Long, Category> categoriesById) {
        List<BudgetRuleDTO> rules = rulesByBudgetId.getOrDefault(budget.getId(), List.of()).stream()
                .sorted(Comparator.comparing(BudgetRuleDTO::getThresholdPercentage, Comparator.nullsLast(BigDecimal::compareTo)))
                .toList();
        Category category = budget.getCategoryId() == null ? null : categoriesById.get(budget.getCategoryId());
        return LedgerBudgetDTO.builder()
                .id(budget.getId())
                .ledgerId(budget.getLedgerId())
                .createdByUserId(budget.getCreatedByUserId())
                .categoryId(budget.getCategoryId())
                .categoryName(category == null ? null : category.getName())
                .name(budget.getName())
                .periodType(budget.getPeriodType())
                .budgetYear(budget.getBudgetYear())
                .budgetMonth(budget.getBudgetMonth())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .type(budget.getType())
                .amount(budget.getAmount())
                .notes(budget.getNotes())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .progress(calculateProgress(budget, rules))
                .rules(rules)
                .build();
    }

    private BudgetProgressDTO calculateProgress(Budget budget, List<BudgetRuleDTO> rules) {
        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ledger_id", budget.getLedgerId())
                .eq("type", budget.getType())
                .ge("transaction_date", budget.getStartDate())
                .le("transaction_date", budget.getEndDate());
        if (budget.getCategoryId() != null) {
            queryWrapper.eq("category_id", budget.getCategoryId());
        }

        BigDecimal spentAmount = moneyKeeperService.list(queryWrapper).stream()
                .map(MoneyKeeper::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal usagePercentage = budget.getAmount() != null && budget.getAmount().signum() > 0
                ? spentAmount.multiply(BigDecimal.valueOf(100)).divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        List<BigDecimal> triggeredThresholds = rules.stream()
                .filter(BudgetRuleDTO::isEnabled)
                .map(BudgetRuleDTO::getThresholdPercentage)
                .filter(Objects::nonNull)
                .filter(threshold -> usagePercentage.compareTo(threshold) >= 0)
                .sorted()
                .toList();

        return BudgetProgressDTO.builder()
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .usagePercentage(usagePercentage)
                .exceeded(remainingAmount.signum() < 0)
                .triggeredThresholdPercentages(triggeredThresholds)
                .build();
    }

    private Budget requireBudget(Long ledgerId, Long budgetId) {
        if (budgetId == null) {
            throw new BadRequestException("Budget id is required");
        }
        Budget budget = getById(budgetId);
        if (budget == null || !ledgerId.equals(budget.getLedgerId())) {
            throw new ResourceNotFoundException("Budget not found");
        }
        return budget;
    }

    private BudgetRule requireRule(Long budgetId, Long ruleId) {
        if (ruleId == null) {
            throw new BadRequestException("Budget rule id is required");
        }
        BudgetRule rule = budgetRuleMapper.selectById(ruleId);
        if (rule == null || !budgetId.equals(rule.getBudgetId())) {
            throw new ResourceNotFoundException("Budget rule not found");
        }
        return rule;
    }

    private Category validateCategory(Long ledgerId, Long categoryId, String type) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryService.getById(categoryId);
        if (category == null || !ledgerId.equals(category.getLedgerId())) {
            throw new BadRequestException("Category must belong to the current ledger");
        }
        if (!type.equals(category.getType())) {
            throw new BadRequestException("Budget type must match the selected category type");
        }
        return category;
    }

    private void ensureNoDuplicateBudget(Long ledgerId,
                                         Long budgetId,
                                         Integer year,
                                         Integer month,
                                         String type,
                                         Long categoryId) {
        QueryWrapper<Budget> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ledger_id", ledgerId)
                .eq("period_type", PERIOD_TYPE_MONTHLY)
                .eq("budget_year", year)
                .eq("budget_month", month)
                .eq("type", type);
        if (categoryId == null) {
            queryWrapper.isNull("category_id");
        } else {
            queryWrapper.eq("category_id", categoryId);
        }
        if (budgetId != null) {
            queryWrapper.ne("id", budgetId);
        }
        if (count(queryWrapper) > 0) {
            throw new ConflictException("A budget already exists for the same month, type, and category scope");
        }
    }

    private void validateUpdateRequest(BudgetRequest request) {
        if (request.getName() == null
                && request.getCategoryId() == null
                && request.getType() == null
                && request.getAmount() == null
                && request.getBudgetYear() == null
                && request.getBudgetMonth() == null
                && request.getNotes() == null) {
            throw new BadRequestException("At least one budget field must be provided");
        }
    }

    private String normalizeRequiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new BadRequestException("Budget type is required");
        }
        String normalizedType = type.trim().toLowerCase();
        if (!"income".equals(normalizedType) && !"expense".equals(normalizedType)) {
            throw new BadRequestException("Budget type must be income or expense");
        }
        return normalizedType;
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Budget amount is required");
        }
        if (amount.signum() <= 0) {
            throw new BadRequestException("Budget amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private int validateYear(Integer year) {
        if (year == null) {
            throw new BadRequestException("Budget year is required");
        }
        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Budget year must be between 2000 and 2100");
        }
        return year;
    }

    private int validateMonth(Integer month) {
        if (month == null) {
            throw new BadRequestException("Budget month is required");
        }
        if (month < 1 || month > 12) {
            throw new BadRequestException("Budget month must be between 1 and 12");
        }
        return month;
    }

    private BigDecimal validateThreshold(BigDecimal thresholdPercentage) {
        if (thresholdPercentage == null) {
            throw new BadRequestException("Threshold percentage is required");
        }
        if (thresholdPercentage.compareTo(BigDecimal.ZERO) <= 0
                || thresholdPercentage.compareTo(new BigDecimal("200")) > 0) {
            throw new BadRequestException("Threshold percentage must be between 0 and 200");
        }
        return thresholdPercentage.setScale(2, RoundingMode.HALF_UP);
    }

    private int resolveEnabled(Boolean enabled) {
        return Boolean.FALSE.equals(enabled) ? 0 : 1;
    }

    private BudgetRuleDTO toRuleDto(BudgetRule rule) {
        return BudgetRuleDTO.builder()
                .id(rule.getId())
                .budgetId(rule.getBudgetId())
                .ruleType(rule.getRuleType())
                .thresholdPercentage(rule.getThresholdPercentage())
                .enabled(rule.getEnabled() != null && rule.getEnabled() == 1)
                .notificationTitle(rule.getNotificationTitle())
                .notificationMessage(rule.getNotificationMessage())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}