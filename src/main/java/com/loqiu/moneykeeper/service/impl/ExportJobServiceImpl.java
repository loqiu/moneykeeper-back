package com.loqiu.moneykeeper.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.config.ExportJobProperties;
import com.loqiu.moneykeeper.dto.ExportJobDTO;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.ExportJob;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ConflictException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.mapper.ExportJobMapper;
import com.loqiu.moneykeeper.service.ExportJobEventDispatcher;
import com.loqiu.moneykeeper.service.ExportJobService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.vo.ExportJobRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class ExportJobServiceImpl implements ExportJobService {

    private static final Logger logger = LogManager.getLogger(ExportJobServiceImpl.class);

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_FAILED = "failed";
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    @Autowired
    private ExportJobMapper exportJobMapper;

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private ExportJobProperties exportJobProperties;

    @Autowired
    private ExportJobEventDispatcher exportJobEventDispatcher;

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
        validateTargetUser(ledgerId, safeRequest.getUserId());

        LocalDateTime queuedAt = LocalDateTime.now();
        ExportJob job = ExportJob.builder()
                .ledgerId(ledgerId)
                .requestedByUserId(requestedByUserId)
                .targetUserId(safeRequest.getUserId())
                .recordType(recordType)
                .startDate(safeRequest.getStartDate())
                .endDate(safeRequest.getEndDate())
                .fileName(buildFileName(ledgerId, queuedAt))
                .fileFormat("xlsx")
                .status(STATUS_PENDING)
                .recordCount(0)
                .downloadCount(0)
                .build();
        exportJobMapper.insert(job);

        notificationService.sendInfoMessage(
                requestedByUserId,
                "Ledger export queued",
                String.format("Your export for ledger %d has been queued. We'll notify you when it's ready.", ledgerId)
        );
        exportJobEventDispatcher.dispatchCreated(job.getId(), ledgerId, requestedByUserId);

        return toDto(job);
    }

    @Override
    public void markJobDownloaded(Long jobId) {
        ExportJob job = exportJobMapper.selectById(jobId);
        if (job == null) {
            throw new ResourceNotFoundException("Export job not found");
        }
        if (!STATUS_COMPLETED.equalsIgnoreCase(job.getStatus())) {
            throw new ConflictException("Export job is not ready to download");
        }
        int currentCount = job.getDownloadCount() == null ? 0 : job.getDownloadCount();
        job.setDownloadCount(currentCount + 1);
        job.setLastDownloadedAt(LocalDateTime.now());
        exportJobMapper.updateById(job);
    }

    @Scheduled(fixedDelayString = "#{@exportJobProperties.pollIntervalMs}")
    public void processPendingJobs() {
        int limit = Math.max(1, exportJobProperties.getMaxJobsPerRun());
        QueryWrapper<ExportJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", STATUS_PENDING)
                .orderByAsc("created_at")
                .orderByAsc("id")
                .last("limit " + limit);

        for (ExportJob pendingJob : exportJobMapper.selectList(queryWrapper)) {
            processPendingJobIfClaimed(pendingJob.getId());
        }
    }

    @Override
    public boolean processPendingJobIfClaimed(Long jobId) {
        if (!claimJobForProcessing(jobId)) {
            return false;
        }
        processJob(jobId);
        return true;
    }

    private boolean claimJobForProcessing(Long jobId) {
        UpdateWrapper<ExportJob> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", jobId)
                .eq("status", STATUS_PENDING)
                .set("status", STATUS_RUNNING)
                .set("started_at", LocalDateTime.now())
                .set("completed_at", null)
                .set("error_message", null)
                .set("storage_path", null);
        return exportJobMapper.update(null, updateWrapper) > 0;
    }

    private void processJob(Long jobId) {
        ExportJob job = exportJobMapper.selectById(jobId);
        if (job == null) {
            return;
        }

        Path storagePath = null;
        try {
            List<MoneyKeeperDTO> records = getRecords(
                    job.getLedgerId(),
                    job.getTargetUserId(),
                    job.getStartDate(),
                    job.getEndDate(),
                    job.getRecordType()
            );

            Path storageDirectory = ensureStorageDirectory();
            storagePath = storageDirectory.resolve(buildStorageFileName(job));
            Files.deleteIfExists(storagePath);
            try (OutputStream outputStream = Files.newOutputStream(storagePath)) {
                EasyExcel.write(outputStream, MoneyKeeperDTO.class)
                        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                        .sheet("Ledger Export")
                        .doWrite(records);
            }

            job.setStatus(STATUS_COMPLETED);
            job.setStoragePath(storagePath.toAbsolutePath().toString());
            job.setErrorMessage(null);
            job.setRecordCount(records.size());
            job.setCompletedAt(LocalDateTime.now());
            exportJobMapper.updateById(job);

            notificationService.sendInfoMessage(
                    job.getRequestedByUserId(),
                    "Ledger export ready",
                    String.format("Your export for ledger %d is ready to download (%d records).", job.getLedgerId(), records.size())
            );
        } catch (Exception e) {
            if (storagePath != null) {
                try {
                    Files.deleteIfExists(storagePath);
                } catch (IOException deleteException) {
                    logger.warn("Failed to delete incomplete export file - jobId: {}, path: {}, error: {}",
                            jobId,
                            storagePath,
                            deleteException.getMessage());
                }
            }

            logger.error("Failed to process export job - jobId: {}, ledgerId: {}, error: {}",
                    jobId,
                    job.getLedgerId(),
                    e.getMessage(),
                    e);

            job.setStatus(STATUS_FAILED);
            job.setStoragePath(null);
            job.setErrorMessage(truncateErrorMessage(e.getMessage()));
            job.setCompletedAt(null);
            exportJobMapper.updateById(job);

            notificationService.sendErrorMessage(
                    job.getRequestedByUserId(),
                    "Ledger export failed",
                    String.format("Your export for ledger %d failed. %s", job.getLedgerId(), defaultIfBlank(job.getErrorMessage(), "Please try again."))
            );
        }
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

    private void validateTargetUser(Long ledgerId, Long targetUserId) {
        if (targetUserId == null) {
            return;
        }
        if (!ledgerService.hasActiveMembership(ledgerId, targetUserId)) {
            throw new BadRequestException("Target user is not an active member of this ledger");
        }
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

    private Path ensureStorageDirectory() throws IOException {
        String configuredStorageDir = trimToNull(exportJobProperties.getStorageDir());
        if (configuredStorageDir == null) {
            throw new IllegalStateException("Export job storage directory is not configured");
        }
        Path storageDirectory = Path.of(configuredStorageDir);
        Files.createDirectories(storageDirectory);
        return storageDirectory;
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
                .storagePath(job.getStoragePath())
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .recordCount(job.getRecordCount())
                .downloadCount(job.getDownloadCount())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .lastDownloadedAt(job.getLastDownloadedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .downloadUrl(String.format("/api/ledgers/%d/export-jobs/%d/download", job.getLedgerId(), job.getId()))
                .build();
    }

    private String buildFileName(Long ledgerId, LocalDateTime queuedAt) {
        return String.format(
                "ledger_%d_records_%s.xlsx",
                ledgerId,
                queuedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        );
    }

    private String buildStorageFileName(ExportJob job) {
        return String.format("export-job-%d.%s", job.getId(), defaultIfBlank(job.getFileFormat(), "xlsx"));
    }

    private String truncateErrorMessage(String errorMessage) {
        String normalizedMessage = defaultIfBlank(trimToNull(errorMessage), "Unknown export error");
        if (normalizedMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return normalizedMessage;
        }
        return normalizedMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
