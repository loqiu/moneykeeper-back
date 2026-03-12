package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.event.ExportJobEvent;
import com.loqiu.moneykeeper.service.ExportJobEventHandler;
import com.loqiu.moneykeeper.service.ExportJobService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExportJobEventHandlerImpl implements ExportJobEventHandler {

    private static final Logger logger = LogManager.getLogger(ExportJobEventHandlerImpl.class);

    @Autowired
    private ExportJobService exportJobService;

    @Override
    public void handle(ExportJobEvent event) {
        if (event == null || event.getJobId() == null) {
            return;
        }
        exportJobService.processPendingJobIfClaimed(event.getJobId());
        logger.info("Handled export job event - eventId: {}, jobId: {}", event.getEventId(), event.getJobId());
    }
}
