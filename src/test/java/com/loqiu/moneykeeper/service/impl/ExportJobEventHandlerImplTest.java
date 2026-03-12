package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.event.ExportJobEvent;
import com.loqiu.moneykeeper.service.ExportJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExportJobEventHandlerImplTest {

    @Mock
    private ExportJobService exportJobService;

    private ExportJobEventHandlerImpl exportJobEventHandler;

    @BeforeEach
    void setUp() {
        exportJobEventHandler = new ExportJobEventHandlerImpl();
        ReflectionTestUtils.setField(exportJobEventHandler, "exportJobService", exportJobService);
    }

    @Test
    void handleShouldProcessPendingJobById() {
        exportJobEventHandler.handle(ExportJobEvent.created(9L, 31L, 2L));

        verify(exportJobService).processPendingJobIfClaimed(9L);
    }
}
