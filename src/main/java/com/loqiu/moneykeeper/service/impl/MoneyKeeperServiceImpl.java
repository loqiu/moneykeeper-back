package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loqiu.moneykeeper.constant.NotificationEventKeyConstants;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.mapper.MoneyKeeperMapper;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.vo.RecordSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class MoneyKeeperServiceImpl extends ServiceImpl<MoneyKeeperMapper, MoneyKeeper> implements MoneyKeeperService {

    private static final Logger logger = LogManager.getLogger(MoneyKeeperServiceImpl.class);
    private static final String BALANCE_WARNING_COUNT_KEY = "balance:warning:count:";
    private static final int MAX_WARNING_COUNT = 3;
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("0.25");

    @Autowired
    private MoneyKeeperMapper moneyKeeperMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public List<MoneyKeeper> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .ge("transaction_date", startDate)
                .le("transaction_date", endDate)
                .orderByDesc("transaction_date");
        return list(queryWrapper);
    }

    @Override
    public List<MoneyKeeper> findByUserIdAndType(Long userId, String type) {
        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("type", type)
                .orderByDesc("transaction_date");
        return list(queryWrapper);
    }

    @Override
    public List<MoneyKeeperDTO> getAllRecordsWithCategoryName(Long userId, LocalDate startDate, LocalDate endDate) {
        return moneyKeeperMapper.getAllRecordsWithCategoryName(userId, startDate, endDate);
    }

    @Override
    public List<MoneyKeeperDTO> getAllLedgerRecordsWithCategoryName(Long ledgerId,
                                                                     Long userId,
                                                                     LocalDate startDate,
                                                                     LocalDate endDate) {
        return moneyKeeperMapper.getLedgerRecordsWithCategoryName(ledgerId, userId, startDate, endDate);
    }

    @Override
    public List<MoneyKeeperDTO> getAllRecordsByCategoryName(String categoryName, Long userId, LocalDate startDate, LocalDate endDate) {
        return moneyKeeperMapper.getAllRecordsByCategoryName(categoryName, userId, startDate, endDate);
    }

    @Override
    public RecordSummary getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, BigDecimal> summary = moneyKeeperMapper.getMoneyKeeperSummary(userId, startDate, endDate);
        logger.info("Summary result - userId: {}, summary: {}", userId, summary);

        BigDecimal totalIncome = summary.getOrDefault("totalIncome", BigDecimal.ZERO);
        BigDecimal totalExpense = summary.getOrDefault("totalExpense", BigDecimal.ZERO);
        BigDecimal balance = summary.getOrDefault("balance", BigDecimal.ZERO);

        checkAndSendBalanceWarning(userId, totalIncome, balance);

        return RecordSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }

    @Override
    public Boolean insertMoneyKeeper(MoneyKeeper moneyKeeper) {
        return moneyKeeperMapper.insertMoneyKeeper(moneyKeeper);
    }

    private void checkAndSendBalanceWarning(Long userId, BigDecimal totalIncome, BigDecimal balance) {
        if (totalIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal ratio = balance.divide(totalIncome, 4, RoundingMode.HALF_UP);
        if (ratio.compareTo(WARNING_THRESHOLD) <= 0) {
            String warningCountKey = BALANCE_WARNING_COUNT_KEY + userId;
            String countStr = redisTemplate.opsForValue().get(warningCountKey);
            int count = countStr == null ? 0 : Integer.parseInt(countStr);

            if (count < MAX_WARNING_COUNT) {
                String warningMessage = String.format(
                        "Your balance (%s) is below 25%% of your total income (%s). Please review your spending.",
                        balance,
                        totalIncome
                );

                try {
                    notificationService.sendWarningMessage(
                            userId,
                            "Balance warning",
                            warningMessage,
                            NotificationEventKeyConstants.RECORD_BALANCE_WARNING,
                            Map.of("userId", userId, "balance", balance, "totalIncome", totalIncome)
                    );
                    redisTemplate.opsForValue().set(warningCountKey, String.valueOf(count + 1));
                    logger.info("Balance warning sent - userId: {}, count: {}/{}", userId, count + 1, MAX_WARNING_COUNT);
                } catch (Exception e) {
                    logger.error("Failed to send balance warning - userId: {}, error: {}", userId, e.getMessage());
                }
            }
        } else {
            redisTemplate.delete(BALANCE_WARNING_COUNT_KEY + userId);
            logger.info("Balance recovered, warning counter reset - userId: {}", userId);
        }
    }
}
