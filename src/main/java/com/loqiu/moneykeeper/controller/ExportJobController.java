package com.loqiu.moneykeeper.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.loqiu.moneykeeper.dto.ExportJobDTO;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.service.ExportJobService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.ExportJobRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ledgers/{ledgerId}/export-jobs")
public class ExportJobController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @Autowired
    private ExportJobService exportJobService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @GetMapping
    public ResponseEntity<List<ExportJobDTO>> listJobs(@PathVariable Long ledgerId,
                                                       @RequestParam(required = false) Integer limit,
                                                       HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return ResponseEntity.ok(exportJobService.listJobs(ledgerId, currentUserId, RequestAuthUtil.isAdmin(request), normalizeLimit(limit)));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ExportJobDTO> getJob(@PathVariable Long ledgerId,
                                               @PathVariable Long jobId,
                                               HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return ResponseEntity.ok(exportJobService.getJob(ledgerId, jobId, currentUserId, RequestAuthUtil.isAdmin(request)));
    }

    @PostMapping
    public ResponseEntity<ExportJobDTO> createJob(@PathVariable Long ledgerId,
                                                  @RequestBody(required = false) ExportJobRequest requestBody,
                                                  HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        validateOptionalDateRange(requestBody == null ? null : requestBody.getStartDate(), requestBody == null ? null : requestBody.getEndDate());
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return ResponseEntity.ok(exportJobService.createJob(ledgerId, currentUserId, requestBody));
    }

    @GetMapping("/{jobId}/download")
    public void downloadJob(@PathVariable Long ledgerId,
                            @PathVariable Long jobId,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        requireLedgerViewer(request, ledgerId);
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        ExportJobDTO job = exportJobService.getJob(ledgerId, jobId, currentUserId, RequestAuthUtil.isAdmin(request));

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String fileName = URLEncoder.encode(job.getFileName(), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);

            List<MoneyKeeperDTO> records = moneyKeeperService.getAllLedgerRecordsWithCategoryName(
                    ledgerId,
                    job.getTargetUserId(),
                    job.getStartDate(),
                    job.getEndDate()
            );
            if (job.getRecordType() != null) {
                records = records.stream().filter(item -> job.getRecordType().equals(item.getType())).toList();
            }

            EasyExcel.write(response.getOutputStream(), MoneyKeeperDTO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("Ledger Export")
                    .doWrite(records);

            exportJobService.markJobDownloaded(jobId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
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

    private void validateOptionalDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }
    }

    private int normalizeLimit(Integer requestedLimit) {
        int limit = requestedLimit == null ? DEFAULT_LIMIT : requestedLimit;
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }
        return limit;
    }
}
