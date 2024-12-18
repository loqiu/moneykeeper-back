package com.loqiu.moneykeeper;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NacosTest {

    @Test
    public void testNacosConnection() throws NacosException {
        // 配置 Nacos 连接信息
        Properties properties = new Properties();
        properties.setProperty("serverAddr", "13.41.222.170:8848");
        ConfigService configService = NacosFactory.createConfigService(properties);

        // 测试是否能够获取服务
        assertNotNull(configService, "Nacos ConfigService should not be null");

        // 测试是否能够获取一个配置
        String dataId = "test-data-id";
        String group = "DEFAULT_GROUP";
        String content = configService.getConfig(dataId, group, 3000);
        System.out.println("Config content: " + content);
    }
}
