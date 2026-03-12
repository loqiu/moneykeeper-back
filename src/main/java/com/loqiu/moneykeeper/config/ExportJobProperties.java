package com.loqiu.moneykeeper.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.export-jobs")
public class ExportJobProperties {
    private String storageDir = System.getProperty("java.io.tmpdir") + "/moneykeeper-exports";
    private long pollIntervalMs = 5000L;
    private int maxJobsPerRun = 3;
}
