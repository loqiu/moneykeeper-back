package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        List<MoneyKeeperDTO> result = moneyKeeperMapper.getAllRecordsWithCategoryName(userId, startDate, endDate);
        return result;
    }

    @Override
    public List<MoneyKeeperDTO> getAllRecordsByCategoryName(String categoryName,Long userId, LocalDate startDate, LocalDate endDate) {
        List<MoneyKeeperDTO> result = moneyKeeperMapper.getAllRecordsByCategoryName(categoryName,userId, startDate, endDate);
        return result;
    }


    @Override
    public RecordSummary getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取汇总数据
        Map<String, BigDecimal> summary = moneyKeeperMapper.getMoneyKeeperSummary(userId, startDate, endDate);
        logger.info("summary result - 用户ID: {}, summary: {}, ", userId, summary);

        BigDecimal totalIncome = summary.get("totalIncome");
        BigDecimal totalExpense = summary.get("totalExpense");
        BigDecimal balance = summary.get("balance");

        // 检查是否需要发送余额预警
        checkAndSendBalanceWarning(userId, totalIncome, balance);

        return RecordSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }

    private void checkAndSendBalanceWarning(Long userId, BigDecimal totalIncome, BigDecimal balance) {
        if (totalIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return; // 如果没有收入，直接返回
        }

        // 计算余额占收入的比例
        BigDecimal ratio = balance.divide(totalIncome, 4, RoundingMode.HALF_UP);
        
        // 如果余额低于收入的25%
        if (ratio.compareTo(WARNING_THRESHOLD) <= 0) {
            String warningCountKey = BALANCE_WARNING_COUNT_KEY + userId;
            String countStr = redisTemplate.opsForValue().get(warningCountKey);
            int count = countStr == null ? 0 : Integer.parseInt(countStr);

            if (count < MAX_WARNING_COUNT) {
                // 发送预警消息
                String warningMessage = String.format(
                    "您的余额(%s)已低于总收入(%s)的25%%，请注意控制支出！",
                    balance.toString(),
                    totalIncome.toString()
                );

                try {
                    notificationService.sendWarningMessage(
                        userId,
                        "余额预警",
                        warningMessage
                    );
                    
                    // 更新计数器
                    count++;
                    redisTemplate.opsForValue().set(warningCountKey, String.valueOf(count));
                    
                    logger.info("已发送余额预警消息 - 用户ID: {}, 当前次数: {}/{}", 
                        userId, count, MAX_WARNING_COUNT);
                } catch (Exception e) {
                    logger.error("发送余额预警消息失败 - 用户ID: {}, 错误: {}", 
                        userId, e.getMessage());
                }
            }
        } else {
            // 如果余额恢复到25%以上，重置计数器
            redisTemplate.delete(BALANCE_WARNING_COUNT_KEY + userId);
            logger.info("余额已恢复正常水平，重置预警计数器 - 用户ID: {}", userId);
        }
    }
} 