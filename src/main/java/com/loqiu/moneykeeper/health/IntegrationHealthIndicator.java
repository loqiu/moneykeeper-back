package com.loqiu.moneykeeper.health;

import com.loqiu.moneykeeper.dto.IntegrationModuleStatusDTO;
import com.loqiu.moneykeeper.service.IntegrationStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("integrations")
public class IntegrationHealthIndicator implements HealthIndicator {

    @Autowired
    private IntegrationStatusService integrationStatusService;

    @Override
    public Health health() {
        List<IntegrationModuleStatusDTO> statuses = integrationStatusService.getAllStatuses();
        long enabledCount = statuses.stream().filter(IntegrationModuleStatusDTO::isEnabled).count();
        long readyCount = statuses.stream().filter(status -> status.isEnabled() && status.isReady()).count();
        boolean allEnabledModulesReady = statuses.stream()
                .filter(IntegrationModuleStatusDTO::isEnabled)
                .allMatch(IntegrationModuleStatusDTO::isReady);

        Health.Builder builder = allEnabledModulesReady ? Health.up() : Health.down();
        builder.withDetail("enabledCount", enabledCount);
        builder.withDetail("readyCount", readyCount);

        for (IntegrationModuleStatusDTO status : statuses) {
            builder.withDetail(status.getModule(), toHealthDetail(status));
        }

        return builder.build();
    }

    private Map<String, Object> toHealthDetail(IntegrationModuleStatusDTO status) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("enabled", status.isEnabled());
        detail.put("ready", status.isReady());
        detail.put("implemented", status.isImplemented());
        detail.put("summary", status.getSummary());
        if (status.getMetadata() != null && !status.getMetadata().isEmpty()) {
            detail.put("metadata", status.getMetadata());
        }
        return detail;
    }
}
