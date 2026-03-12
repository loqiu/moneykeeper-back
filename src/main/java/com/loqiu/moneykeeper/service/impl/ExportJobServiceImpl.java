package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.loqiu.moneykeeper.dto.ExportJobDTO;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.ExportJob;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.mapper.ExportJobMapper;
import com.loqiu.moneykeeper.service.ExportJobService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.vo.ExportJobRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class ExportJobServiceImpl implements ExportJobService {

    @Autowired
    private ExportJobMapper exportJobMapper;

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<ExportJobDTO> listJobs(Long ledgerId, Long currentUserId, boolean admin, int limit) {
        QueryWrapper<ExportJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ledger_id", ledgerId)
                .orderByDesc("created_at")
                .orderByDesc("id")
                .last("limit " + limit);
        if (!admin) {
            queryWrapper.eq("requested_by_user_id", currentUserId);
        }
        return exportJobMapper.selectList(queryWrapper).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ExportJobDTO getJob(Long ledgerId, Long jobId, Long currentUserId, boolean admin) {
        return toDto(requireJob(ledgerId, jobId, currentUserId, admin));
    }

    @Override
    public ExportJobDTO createJob(Long ledgerId, Long requestedByUserId, ExportJobRequest request) {
        ExportJobRequest safeRequest = request == null ? new ExportJobRequest() : request;
        String recordType = trimToNull(safeRequest.getType());
        List<MoneyKeeperDTO> records = getRecords(ledgerId, safeRequest.getUserId(), safeRequest.getStartDate(), safeRequest.getEndDate(), recordType);

        ExportJob job = ExportJob.builder()
                .ledgerId(ledgerId)
                .requestedByUserId(requestedByUserId)
                .targetUserId(safeRequest.getUserId())
                .recordType(recordType)
                .startDate(safeRequest.getStartDate())
                .endDate(safeRequest.getEndDate())
                .fileName(buildFileName(ledgerId))
                .fileFormat("xlsx")
                .status("completed")
                .recordCount(records.size())
                .downloadCount(0)
                .completedAt(LocalDateTime.now())
                .build();
        exportJobMapper.insert(job);

        notificationService.sendInfoMessage(
                requestedByUserId,
                "Ledger export ready",
                String.format("Your export for ledger %d is ready to download (%d records).", ledgerId, records.size())
        );

        return toDto(job);
    }

    @Override
    public void markJobDownloaded(Long jobId) {
        ExportJob job = exportJobMapper.selectById(jobId);
        if (job == null) {
            throw new ResourceNotFoundException("Export job not found");
        }
        int currentCount = job.getDownloadCount() == null ? 0 : job.getDownloadCount();
        job.setDownloadCount(currentCount + 1);
        job.setLastDownloadedAt(LocalDateTime.now());
        exportJobMapper.updateById(job);
    }

    private ExportJob requireJob(Long ledgerId, Long jobId, Long currentUserId, boolean admin) {
        QueryWrapper<ExportJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", jobId)
                .eq("ledger_id", ledgerId);
        ExportJob job = exportJobMapper.selectOne(queryWrapper);
        if (job == null) {
            throw new ResourceNotFoundException("Export job not found");
        }
        if (!admin && !Objects.equals(job.getRequestedByUserId(), currentUserId)) {
            throw new ForbiddenException("You do not have permission to access this export job");
        }
        return job;
    }

    private List<MoneyKeeperDTO> getRecords(Long ledgerId,
                                            Long userId,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            String recordType) {
        List<MoneyKeeperDTO> records = moneyKeeperService.getAllLedgerRecordsWithCategoryName(ledgerId, userId, startDate, endDate);
        if (recordType == null) {
            return records;
        }
        return records.stream()
                .filter(item -> recordType.equals(item.getType()))
                .toList();
    }

    private ExportJobDTO toDto(ExportJob job) {
        return ExportJobDTO.builder()
                .id(job.getId())
                .ledgerId(job.getLedgerId())
                .requestedByUserId(job.getRequestedByUserId())
                .targetUserId(job.getTargetUserId())
                .recordType(job.getRecordType())
                .startDate(job.getStartDate())
                .endDate(job.getEndDate())
                .fileName(job.getFileName())
                .fileFormat(job.getFileFormat())
                .status(job.getStatus())
                .recordCount(job.getRecordCount())
                .downloadCount(job.getDownloadCount())
                .completedAt(job.getCompletedAt())
                .lastDownloadedAt(job.getLastDownloadedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .downloadUrl(String.format("/api/ledgers/%d/export-jobs/%d/download", job.getLedgerId(), job.getId()))
                .build();
    }

    private String buildFileName(Long ledgerId) {
        return String.format("ledger_%d_records_%s.xlsx", ledgerId, LocalDate.now().format(DateTimeFormatter.ISO_DATE));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
