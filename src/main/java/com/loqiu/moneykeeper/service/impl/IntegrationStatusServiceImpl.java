package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.config.DubboModuleProperties;
import com.loqiu.moneykeeper.config.ElasticsearchProperties;
import com.loqiu.moneykeeper.config.KafkaFeatureProperties;
import com.loqiu.moneykeeper.config.NacosConfigProperties;
import com.loqiu.moneykeeper.config.NacosDiscoveryProperties;
import com.loqiu.moneykeeper.config.PaymentProperties;
import com.loqiu.moneykeeper.dto.IntegrationModuleStatusDTO;
import com.loqiu.moneykeeper.service.IntegrationStatusService;
import com.loqiu.moneykeeper.service.KafkaConsumerService;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import com.loqiu.moneykeeper.service.PaymentStripeService;
import com.loqiu.moneykeeper.service.RecordSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IntegrationStatusServiceImpl implements IntegrationStatusService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private KafkaFeatureProperties kafkaFeatureProperties;

    @Autowired
    private DubboModuleProperties dubboModuleProperties;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @Autowired
    private PaymentStripeService paymentStripeService;

    @Autowired
    private PaymentProperties paymentProperties;

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Autowired
    private RecordSearchService recordSearchService;

    @Override
    public List<IntegrationModuleStatusDTO> getAllStatuses() {
        Map<String, IntegrationModuleStatusDTO> statuses = buildStatuses();
        return List.copyOf(statuses.values());
    }

    @Override
    public IntegrationModuleStatusDTO getStatus(String moduleName) {
        if (moduleName == null) {
            return null;
        }
        return buildStatuses().get(moduleName.trim().toLowerCase());
    }

    private Map<String, IntegrationModuleStatusDTO> buildStatuses() {
        Map<String, IntegrationModuleStatusDTO> statuses = new LinkedHashMap<>();
        statuses.put("kafka", buildKafkaStatus());
        statuses.put("elasticsearch", buildElasticsearchStatus());
        statuses.put("payment", buildPaymentStatus());
        statuses.put("dubbo", buildDubboStatus());
        statuses.put("nacos-discovery", buildNacosDiscoveryStatus());
        statuses.put("nacos-config", buildNacosConfigStatus());
        return statuses;
    }

    private IntegrationModuleStatusDTO buildKafkaStatus() {
        boolean enabled = kafkaFeatureProperties.isEnabled();
        return IntegrationModuleStatusDTO.builder()
                .module("kafka")
                .enabled(enabled)
                .ready(kafkaProducerService.isEnabled())
                .implemented(true)
                .summary(enabled ? "Kafka producer and consumer scaffolding is enabled" : "Kafka module is disabled via app.kafka.enabled")
                .metadata(Map.of(
                        "producerReady", kafkaProducerService.isEnabled(),
                        "consumerReady", kafkaConsumerService.isEnabled(),
                        "consumedCount", kafkaConsumerService.getConsumedCount(),
                        "bootstrapServers", kafkaFeatureProperties.getBootstrapServers(),
                        "consumerGroupId", kafkaFeatureProperties.getConsumerGroupId(),
                        "listenerAutoStartup", kafkaFeatureProperties.isListenerAutoStartup(),
                        "recordEventTopic", kafkaFeatureProperties.getRecordEventTopic()
                ))
                .build();
    }

    private IntegrationModuleStatusDTO buildElasticsearchStatus() {
        boolean enabled = recordSearchService.isEnabled();
        boolean ready = recordSearchService.isReady();
        return IntegrationModuleStatusDTO.builder()
                .module("elasticsearch")
                .enabled(enabled)
                .ready(ready)
                .implemented(true)
                .summary(enabled
                        ? (ready ? "Elasticsearch record search APIs are enabled" : "Elasticsearch feature is enabled but the client is not ready")
                        : "Elasticsearch module is disabled via app.elasticsearch.enabled")
                .metadata(Map.of(
                        "searchApiAvailable", true,
                        "indexName", elasticsearchProperties.getIndexName(),
                        "host", elasticsearchProperties.getHost(),
                        "port", elasticsearchProperties.getPort()
                ))
                .build();
    }

    private IntegrationModuleStatusDTO buildPaymentStatus() {
        boolean enabled = paymentStripeService.isEnabled();
        boolean ready = paymentStripeService.isReady();
        return IntegrationModuleStatusDTO.builder()
                .module("payment")
                .enabled(enabled)
                .ready(ready)
                .implemented(true)
                .summary(!enabled
                        ? "Payment module is disabled via app.payment.enabled"
                        : (ready
                        ? paymentProperties.getProvider() + " hosted checkout and webhook flow is ready"
                        : paymentProperties.getProvider() + " payment module is enabled but the Stripe secret key or webhook secret is missing"))
                .metadata(Map.of(
                        "provider", paymentProperties.getProvider(),
                        "defaultCurrency", paymentProperties.getDefaultCurrency(),
                        "apiReady", paymentProperties.hasSecretKey(),
                        "webhookReady", paymentProperties.hasWebhookSecret(),
                        "billingPortalReturnUrlConfigured", paymentProperties.hasBillingPortalReturnUrl()
                ))
                .build();
    }

    private IntegrationModuleStatusDTO buildDubboStatus() {
        boolean enabled = dubboModuleProperties.isEnabled();
        boolean ready = applicationContext.getBeanNamesForType(com.loqiu.moneykeeper.config.DubboFeatureConfig.class).length > 0;
        return IntegrationModuleStatusDTO.builder()
                .module("dubbo")
                .enabled(enabled)
                .ready(ready)
                .implemented(false)
                .summary(enabled ? "Dubbo infrastructure is enabled; RPC service contracts are not implemented yet" : "Dubbo module is disabled via app.dubbo.enabled")
                .metadata(Map.of(
                        "applicationName", dubboModuleProperties.getApplicationName(),
                        "registryAddress", dubboModuleProperties.getRegistryAddress()
                ))
                .build();
    }

    private IntegrationModuleStatusDTO buildNacosDiscoveryStatus() {
        boolean enabled = nacosDiscoveryProperties.isEnabled();
        boolean ready = applicationContext.getBeanNamesForType(com.loqiu.moneykeeper.config.NacosDiscoveryFeatureConfig.class).length > 0;
        return IntegrationModuleStatusDTO.builder()
                .module("nacos-discovery")
                .enabled(enabled)
                .ready(ready)
                .implemented(false)
                .summary(enabled ? "Nacos discovery integration is enabled" : "Nacos discovery is disabled")
                .metadata(Map.of(
                        "serverAddr", nacosDiscoveryProperties.getServerAddr()
                ))
                .build();
    }

    private IntegrationModuleStatusDTO buildNacosConfigStatus() {
        boolean enabled = nacosConfigProperties.isEnabled();
        return IntegrationModuleStatusDTO.builder()
                .module("nacos-config")
                .enabled(enabled)
                .ready(enabled)
                .implemented(false)
                .summary(enabled ? "Nacos config integration is enabled" : "Nacos config is disabled")
                .metadata(Map.of(
                        "serverAddr", nacosConfigProperties.getServerAddr()
                ))
                .build();
    }
}
