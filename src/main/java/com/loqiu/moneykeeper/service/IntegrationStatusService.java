package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.dto.IntegrationModuleStatusDTO;

import java.util.List;

public interface IntegrationStatusService {
    List<IntegrationModuleStatusDTO> getAllStatuses();

    IntegrationModuleStatusDTO getStatus(String moduleName);
}