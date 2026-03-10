package com.loqiu.moneykeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.elasticsearch")
public class ElasticsearchProperties {
    private String host = "localhost";
    private int port = 9200;
    private String username;
    private String password;
    private String indexName = "moneykeeper-records";
}