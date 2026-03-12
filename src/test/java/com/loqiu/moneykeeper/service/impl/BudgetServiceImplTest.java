package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.entity.Budget;
import com.loqiu.moneykeeper.entity.BudgetRule;
import com.loqiu.moneykeeper.entity.LedgerMember;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.mapper.BudgetMapper;
import com.loqiu.moneykeeper.mapper.BudgetRuleMapper;
import com.loqiu.moneykeeper.mapper.LedgerMemberMapper;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetMapper budgetMapper;

    @Mock
    private BudgetRuleMapper budgetRuleMapper;

    @Mock
    private MoneyKeeperService moneyKeeperService;

    @Mock
    private LedgerMemberMapper ledgerMemberMapper;

    @Mock
    private NotificationService notificationService;

    private BudgetServiceImpl budgetService;

    private TestRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetServiceImpl();
        redisTemplate = new TestRedisTemplate();
        ReflectionTestUtils.setField(budgetService, "baseMapper", budgetMapper);
        ReflectionTestUtils.setField(budgetService, "budgetRuleMapper", budgetRuleMapper);
        ReflectionTestUtils.setField(budgetService, "moneyKeeperService", moneyKeeperService);
        ReflectionTestUtils.setField(budgetService, "ledgerMemberMapper", ledgerMemberMapper);
        ReflectionTestUtils.setField(budgetService, "notificationService", notificationService);
        ReflectionTestUtils.setField(budgetService, "redisTemplate", redisTemplate);
    }

    @Test
    void syncThresholdNotificationsForLedgerRecordShouldSendWarningOnceThresholdIsReached() {
        Budget budget = buildBudget();
        MoneyKeeper record = buildRecord(new BigDecimal("50.00"));
        BudgetRule rule = BudgetRule.builder()
                .id(71L)
                .budgetId(41L)
                .ruleType("threshold")
                .thresholdPercentage(new BigDecimal("40.00"))
                .enabled(1)
                .notificationTitle("Budget alert")
                .build();
        LedgerMember manager = LedgerMember.builder()
                .ledgerId(31L)
                .userId(7L)
                .role("owner")
                .status("active")
                .build();
        String notificationKey = "budget:threshold:notification:41:71:40.00:user:7";

        when(budgetMapper.selectList(any())).thenReturn(List.of(budget));
        when(budgetRuleMapper.selectList(any())).thenReturn(List.of(rule));
        when(ledgerMemberMapper.selectList(any())).thenReturn(List.of(manager));
        when(moneyKeeperService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<MoneyKeeper>>any())).thenReturn(List.of(record));

        budgetService.syncThresholdNotificationsForLedgerRecord(31L, null, record);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).sendWarningMessage(eq(7L), eq("Budget alert"), messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("50.00%"));
        assertTrue(messageCaptor.getValue().contains("40.00%"));
        assertEquals("50.00", redisTemplate.getValue(notificationKey));
    }

    @Test
    void syncThresholdNotificationsForLedgerRecordShouldClearNotificationKeyWhenUsageDropsBelowThreshold() {
        Budget budget = buildBudget();
        MoneyKeeper record = buildRecord(new BigDecimal("20.00"));
        BudgetRule rule = BudgetRule.builder()
                .id(71L)
                .budgetId(41L)
                .ruleType("threshold")
                .thresholdPercentage(new BigDecimal("40.00"))
                .enabled(1)
                .build();
        LedgerMember manager = LedgerMember.builder()
                .ledgerId(31L)
                .userId(7L)
                .role("owner")
                .status("active")
                .build();
        String notificationKey = "budget:threshold:notification:41:71:40.00:user:7";
        redisTemplate.putValue(notificationKey, "50.00");

        when(budgetMapper.selectList(any())).thenReturn(List.of(budget));
        when(budgetRuleMapper.selectList(any())).thenReturn(List.of(rule));
        when(ledgerMemberMapper.selectList(any())).thenReturn(List.of(manager));
        when(moneyKeeperService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<MoneyKeeper>>any())).thenReturn(List.of(record));

        budgetService.syncThresholdNotificationsForLedgerRecord(31L, null, record);

        verify(notificationService, never()).sendWarningMessage(any(), any(), any());
        assertFalse(redisTemplate.containsKey(notificationKey));
    }

    private Budget buildBudget() {
        return Budget.builder()
                .id(41L)
                .ledgerId(31L)
                .createdByUserId(7L)
                .categoryId(8L)
                .name("March Coffee Budget")
                .periodType("monthly")
                .budgetYear(2026)
                .budgetMonth(3)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .type("expense")
                .amount(new BigDecimal("100.00"))
                .build();
    }

    private MoneyKeeper buildRecord(BigDecimal amount) {
        MoneyKeeper record = new MoneyKeeper();
        record.setId(11L);
        record.setLedgerId(31L);
        record.setUserId(2L);
        record.setCategoryId(8L);
        record.setType("expense");
        record.setAmount(amount);
        record.setTransactionDate(LocalDate.of(2026, 3, 11));
        return record;
    }

    private static final class TestRedisTemplate extends RedisTemplate<String, String> {
        private final Map<String, String> values = new ConcurrentHashMap<>();
        private final ValueOperations<String, String> valueOperations = createValueOperations();

        @Override
        public ValueOperations<String, String> opsForValue() {
            return valueOperations;
        }

        @Override
        public Boolean hasKey(String key) {
            return values.containsKey(key);
        }

        @Override
        public Boolean delete(String key) {
            return values.remove(key) != null;
        }

        private ValueOperations<String, String> createValueOperations() {
            return (ValueOperations<String, String>) Proxy.newProxyInstance(
                    ValueOperations.class.getClassLoader(),
                    new Class[]{ValueOperations.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("set".equals(methodName) && args != null && args.length >= 2) {
                            values.put(String.valueOf(args[0]), (String) args[1]);
                            return null;
                        }
                        if ("get".equals(methodName) && args != null && args.length == 1) {
                            return values.get(String.valueOf(args[0]));
                        }
                        throw new UnsupportedOperationException("Unsupported ValueOperations method: " + methodName);
                    }
            );
        }

        private void putValue(String key, String value) {
            values.put(key, value);
        }

        private String getValue(String key) {
            return values.get(key);
        }

        private boolean containsKey(String key) {
            return values.containsKey(key);
        }
    }
}
