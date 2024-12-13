package com.loqiu.moneykeeper.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchConfig {

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 创建凭证提供器
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(
                elasticsearchProperties.getUsername(),
                elasticsearchProperties.getPassword()
            )
        );

        // 创建低级客户端
        RestClient restClient = RestClient.builder(
            new HttpHost(
                elasticsearchProperties.getHost(),
                elasticsearchProperties.getPort(),
                "http"
            )
        )
        .setHttpClientConfigCallback(httpAsyncClientBuilder -> 
            httpAsyncClientBuilder.setDefaultCredentialsProvider(credsProv)
        )
        .build();

        // 创建传输层
        ElasticsearchTransport transport = new RestClientTransport(
            restClient, 
            new JacksonJsonpMapper()
        );

        // 创建API客户端
        return new ElasticsearchClient(transport);
    }
} 