package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationModuleStatusDTO {
    private String module;
    private boolean enabled;
    private boolean ready;
    private boolean implemented;
    private String summary;
    private Map<String, Object> metadata;
}