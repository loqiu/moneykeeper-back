package com.loqiu.moneykeeper.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.util.RecordTypeNormalizer;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "Excel Export", description = "Export money keeper records as Excel files")
@RestController
@RequestMapping("/api/excel")
public class ExcelDownloadController {
    private static final Logger logger = LogManager.getLogger(ExcelDownloadController.class);

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @Autowired
    private LedgerService ledgerService;

    @Operation(summary = "Download user records as Excel")
    @GetMapping("/download/{userId}")
    public void downloadUserRecords(@PathVariable Long userId,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        logger.info("Starting Excel download - targetUserId: {}, currentUserId: {}, type: {}, startDate: {}, endDate: {}",
                userId, RequestAuthUtil.getCurrentUserId(request), type, startDate, endDate);

        requireSelfOrAdmin(request, userId);
        validateOptionalDateRange(startDate, endDate);
        String normalizedType = normalizeRecordType(type);

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String fileName = URLEncoder.encode("records_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName + ".xlsx");

            List<MoneyKeeperDTO> records = moneyKeeperService.getAllRecordsWithCategoryName(userId, startDate, endDate);
            if (normalizedType != null) {
                records = records.stream().filter(item -> normalizedType.equals(item.getType())).toList();
            }

            EasyExcel.write(response.getOutputStream(), MoneyKeeperDTO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("Records")
                    .doWrite(records);

            logger.info("Excel download completed successfully for userId: {}", userId);
        } catch (IOException e) {
            logger.error("Failed to download Excel for userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    @Operation(summary = "Download ledger records as Excel")
    @GetMapping("/ledgers/{ledgerId}/download")
    public void downloadLedgerRecords(@PathVariable Long ledgerId,
                                      @RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        logger.info("Starting ledger Excel download - ledgerId: {}, requestedUserId: {}, currentUserId: {}, type: {}, startDate: {}, endDate: {}",
                ledgerId, userId, RequestAuthUtil.getCurrentUserId(request), type, startDate, endDate);

        requireLedgerViewer(request, ledgerId);
        validateOptionalDateRange(startDate, endDate);
        String normalizedType = normalizeRecordType(type);

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String fileName = URLEncoder.encode("ledger_records_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName + ".xlsx");

            List<MoneyKeeperDTO> records = moneyKeeperService.getAllLedgerRecordsWithCategoryName(ledgerId, userId, startDate, endDate);
            if (normalizedType != null) {
                records = records.stream().filter(item -> normalizedType.equals(item.getType())).toList();
            }

            EasyExcel.write(response.getOutputStream(), MoneyKeeperDTO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("Ledger Records")
                    .doWrite(records);

            logger.info("Ledger Excel download completed successfully for ledgerId: {}", ledgerId);
        } catch (IOException e) {
            logger.error("Failed to download ledger Excel for ledgerId: {}, error: {}", ledgerId, e.getMessage());
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private void requireSelfOrAdmin(HttpServletRequest request, Long userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }
        if (!RequestAuthUtil.isSelfOrAdmin(request, userId)) {
            throw new ForbiddenException("You do not have permission to export this user's records");
        }
    }

    private void requireLedgerViewer(HttpServletRequest request, Long ledgerId) {
        ledgerService.requireLedger(ledgerId);
        if (RequestAuthUtil.isAdmin(request)) {
            return;
        }
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (!ledgerService.hasActiveMembership(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to export this ledger's records");
        }
    }

    private void validateOptionalDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeRecordType(String value) {
        return RecordTypeNormalizer.normalizeOptional(value, "Record type must be income or expense");
    }
}
