package com.loqiu.moneykeeper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {
    private String host = "localhost";
    private int port = 9200;
    private String username;
    private String password;
} 