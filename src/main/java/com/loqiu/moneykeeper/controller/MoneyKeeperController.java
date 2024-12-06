package com.loqiu.moneykeeper.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.DTO.MoneyKeeperDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.service.MoneyKeeperService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "记账记录管理", description = "记账记录的CRUD操作")
@RestController
@RequestMapping("/api/records")
public class MoneyKeeperController {

    private static final Logger logger = LogManager.getLogger(MoneyKeeperController.class);

    @Autowired
    private MoneyKeeperService moneyKeeperService;
    
    @Operation(summary = "创建记账记录") 
    @PostMapping
    public ResponseEntity<MoneyKeeper> createRecord(@RequestBody MoneyKeeper record) {
        logger.info("Creating record - Input - record: {}", record);
        
        if (record == null || record.getUserId() == null || record.getCategoryId() == null 
            || record.getAmount() == null || record.getTransactionDate() == null) {
            logger.error("Invalid input - record data is incomplete: {}", record);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            moneyKeeperService.save(record);
            logger.info("Record created successfully - Output - record: {}", record);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            logger.error("Failed to create record - error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "根据ID获取记账记录")
    @GetMapping("/{id}")
    public ResponseEntity<MoneyKeeper> getRecordById(@PathVariable Long id) {
        logger.info("Getting record - Input - recordId: {}", id);
        
        if (id == null) {
            logger.error("Invalid input - recordId is null");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            MoneyKeeper record = moneyKeeperService.getById(id);
            if (record != null) {
                logger.info("Record found - Output - record: {}", record);
                return ResponseEntity.ok(record);
            } else {
                logger.warn("Record not found - recordId: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get record - recordId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "根据用户ID和日期范围获取记账记录")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MoneyKeeper>> getRecordsByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("Getting records by userId and date range - Input - userId: {}, startDate: {}, endDate: {}", 
                userId, startDate, endDate);
        
        if (userId == null || startDate == null || endDate == null) {
            logger.error("Invalid input - userId: {}, startDate: {}, endDate: {}", userId, startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        if (endDate.isBefore(startDate)) {
            logger.error("Invalid date range - startDate: {} is after endDate: {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<MoneyKeeper> records = moneyKeeperService.findByUserIdAndDateRange(userId, startDate, endDate);
            logger.info("Records found - Output - count: {}", records.size());
            logger.debug("Records details: {}", records);  // 详细记录放在debug级别
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Failed to get records - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "根据用户ID和类型获取记账记录")
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<MoneyKeeper>> getRecordsByUserIdAndType(
            @PathVariable Long userId,
            @PathVariable String type) {
        logger.info("Getting records by userId and type - Input - userId: {}, type: {}", userId, type);
        
        if (userId == null || type == null || type.trim().isEmpty()) {
            logger.error("Invalid input - userId: {}, type: {}", userId, type);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<MoneyKeeper> records = moneyKeeperService.findByUserIdAndType(userId, type);
            logger.info("Records found - Output - count: {}", records.size());
            logger.debug("Records details: {}", records);  // 详细记录放在debug级别
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Failed to get records - userId: {}, type: {}, error: {}", userId, type, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "更新记账记录")
    @PutMapping("/{id}")
    public ResponseEntity<MoneyKeeper> updateRecord(@PathVariable Long id, @RequestBody MoneyKeeper record) {
        logger.info("Updating record - Input - recordId: {}, record: {}", id, record);
        
        if (id == null || record == null) {
            logger.error("Invalid input - recordId: {}, record: {}", id, record);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            MoneyKeeper existingRecord = moneyKeeperService.getById(id);
            if (existingRecord == null) {
                logger.warn("Record not found - recordId: {}", id);
                return ResponseEntity.notFound().build();
            }

            if(null == record.getId()){
                record.setId(id);
            }
            logger.info("Updating record - record: {}", record);
            moneyKeeperService.updateById(record);
            logger.info("Record updated successfully - Output - record: {}", record);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            logger.error("Failed to update record - recordId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "删除记账记录")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        logger.info("Deleting record - Input - recordId: {}", id);
        
        if (id == null) {
            logger.error("Invalid input - recordId is null");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            MoneyKeeper existingRecord = moneyKeeperService.getById(id);
            if (existingRecord == null) {
                logger.warn("Record not found - recordId: {}", id);
                return ResponseEntity.notFound().build();
            }
            LocalDateTime currentTime = LocalDateTime.now();
            UpdateWrapper<MoneyKeeper> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                    .set("deleted_at", 1)
                    .set("deleted_time", currentTime);
            logger.info("Deleting record - record: {}", updateWrapper);
            moneyKeeperService.update(updateWrapper);
            logger.info("Record deleted successfully - recordId: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to delete record - recordId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "获取所有记账记录")
    @GetMapping("/list")
    public ResponseEntity<List<MoneyKeeper>> getAllRecords() {
        logger.info("Getting all records");
        
        try {
            List<MoneyKeeper> records = moneyKeeperService.list();
            logger.info("Records found - Output - count: {}", records.size());
            logger.debug("Records details: {}", records);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Failed to get records list - error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "获取所有记账记录带分类名称")
    @GetMapping("/listWithCategoryName")
    public ResponseEntity<List<MoneyKeeperDTO>> getAllRecordsWithCategoryName() {
        logger.info("Getting records with category names");
        
        try {
            List<MoneyKeeperDTO> records = moneyKeeperService.getAllRecordsWithCategoryName();
            logger.info("Records with category names found - Output - count: {}, record:{}", records.size(), JSON.toJSONString(records));
            logger.debug("Records details: {}", records);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Failed to get records with category names - error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "根据用户ID获取记账记录")
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<MoneyKeeper>> getRecordsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("Getting records by userId - Input - userId: {}, type: {}, startDate: {}, endDate: {}", 
                userId, type, startDate, endDate);
        
        if (userId == null) {
            logger.error("Invalid input - userId is null");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("deleted_at", 0);
            
            // 添加类型过滤
            if (type != null && !type.trim().isEmpty()) {
                queryWrapper.eq("type", type);
            }
            
            // 添加日期范围过滤
            if (startDate != null && endDate != null) {
                if (endDate.isBefore(startDate)) {
                    logger.error("Invalid date range - startDate: {} is after endDate: {}", startDate, endDate);
                    return ResponseEntity.badRequest().build();
                }
                queryWrapper.between("transaction_date", startDate, endDate);
            } else if (startDate != null) {
                queryWrapper.ge("transaction_date", startDate);
            } else if (endDate != null) {
                queryWrapper.le("transaction_date", endDate);
            }
            
            queryWrapper.orderByDesc("transaction_date", "created_at");
            
            List<MoneyKeeper> records = moneyKeeperService.list(queryWrapper);
            logger.info("Records found - Output - count: {}", records.size());
            logger.debug("Records details: {}", records);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Failed to get records - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 