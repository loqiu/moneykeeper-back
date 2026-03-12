package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.config.ExportJobProperties;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.ExportJob;
import com.loqiu.moneykeeper.service.ExportJobEventDispatcher;
import com.loqiu.moneykeeper.mapper.ExportJobMapper;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.vo.ExportJobRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportJobServiceImplTest {

    @Mock
    private ExportJobMapper exportJobMapper;

    @Mock
    private MoneyKeeperService moneyKeeperService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private ExportJobEventDispatcher exportJobEventDispatcher;

    @TempDir
    Path tempDir;

    private ExportJobServiceImpl exportJobService;

    @BeforeEach
    void setUp() {
        exportJobService = new ExportJobServiceImpl();
        ExportJobProperties exportJobProperties = new ExportJobProperties();
        exportJobProperties.setStorageDir(tempDir.toString());
        exportJobProperties.setMaxJobsPerRun(2);
        exportJobProperties.setPollIntervalMs(1000L);

        ReflectionTestUtils.setField(exportJobService, "exportJobMapper", exportJobMapper);
        ReflectionTestUtils.setField(exportJobService, "moneyKeeperService", moneyKeeperService);
        ReflectionTestUtils.setField(exportJobService, "notificationService", notificationService);
        ReflectionTestUtils.setField(exportJobService, "ledgerService", ledgerService);
        ReflectionTestUtils.setField(exportJobService, "exportJobProperties", exportJobProperties);
        ReflectionTestUtils.setField(exportJobService, "exportJobEventDispatcher", exportJobEventDispatcher);
    }

    @Test
    void createJobShouldQueuePendingJobAndNotifyRequester() {
        when(ledgerService.hasActiveMembership(31L, 4L)).thenReturn(true);
        when(exportJobMapper.insert(any(ExportJob.class))).thenAnswer(invocation -> {
            ExportJob job = invocation.getArgument(0);
            job.setId(7L);
            job.setCreatedAt(LocalDateTime.of(2026, 3, 12, 12, 40, 0));
            return 1;
        });

        var job = exportJobService.createJob(31L, 2L, ExportJobRequest.builder()
                .userId(4L)
                .type("expense")
                .build());

        assertEquals(7L, job.getId());
        assertEquals("pending", job.getStatus());
        assertEquals(4L, job.getTargetUserId());
        verify(notificationService).sendInfoMessage(
                eq(2L),
                eq("Ledger export queued"),
                eq("Your export for ledger 31 has been queued. We'll notify you when it's ready.")
        );
        verify(exportJobEventDispatcher).dispatchCreated(7L, 31L, 2L);
    }

    @Test
    void processPendingJobsShouldWriteWorkbookAndMarkJobCompleted() throws Exception {
        ExportJob pendingJob = ExportJob.builder()
                .id(7L)
                .ledgerId(31L)
                .requestedByUserId(2L)
                .targetUserId(4L)
                .recordType("expense")
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .fileName("ledger_31_records_2026-03-12_12-40-00.xlsx")
                .fileFormat("xlsx")
                .status("pending")
                .build();

        when(exportJobMapper.selectList(any())).thenReturn(List.of(pendingJob));
        when(exportJobMapper.update(eq(null), any())).thenReturn(1);
        when(exportJobMapper.selectById(7L)).thenReturn(pendingJob);
        when(moneyKeeperService.getAllLedgerRecordsWithCategoryName(
                31L,
                4L,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        )).thenReturn(List.of(MoneyKeeperDTO.builder()
                .categoryName("Platform Coffee")
                .type("expense")
                .amount(new BigDecimal("12.34"))
                .transactionDate(LocalDate.of(2026, 3, 12))
                .notes("Team coffee")
                .updatedAt(LocalDateTime.of(2026, 3, 12, 12, 41, 0))
                .build()));

        exportJobService.processPendingJobs();

        ArgumentCaptor<ExportJob> updatedJobCaptor = ArgumentCaptor.forClass(ExportJob.class);
        verify(exportJobMapper).updateById(updatedJobCaptor.capture());
        ExportJob completedJob = updatedJobCaptor.getValue();
        assertEquals("completed", completedJob.getStatus());
        assertEquals(1, completedJob.getRecordCount());
        assertTrue(completedJob.getStoragePath().endsWith("export-job-7.xlsx"));
        assertTrue(Files.exists(Path.of(completedJob.getStoragePath())));

        verify(notificationService).sendInfoMessage(
                eq(2L),
                eq("Ledger export ready"),
                eq("Your export for ledger 31 is ready to download (1 records).")
        );
    }
}
