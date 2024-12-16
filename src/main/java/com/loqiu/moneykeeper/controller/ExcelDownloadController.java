package com.loqiu.moneykeeper.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "记账记录管理", description = "记账记录的CRUD操作")
@RestController
@RequestMapping("/api/excel")
public class ExcelDownloadController {
    private static final Logger logger = LogManager.getLogger(ExcelDownloadController.class);

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @Operation(summary = "下载用户记账记录Excel")
    @GetMapping("/download/{userId}")
    public void downloadUserRecords(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletResponse response) {
        logger.info("Starting Excel download for userId: {}, type: {}, startDate: {}, endDate: {}", 
                userId, type, startDate, endDate);

        try {
            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("记账记录_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE), "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 获取数据
            List<MoneyKeeperDTO> records = moneyKeeperService.getAllRecordsWithCategoryName(userId, startDate, endDate);

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), MoneyKeeperDTO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 自动调整列宽
                    .sheet("记账记录")
                    .doWrite(records);

            logger.info("Excel download completed successfully for userId: {}", userId);
        } catch (IOException e) {
            logger.error("Failed to download Excel for userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("下载Excel失败", e);
        }
    }
}