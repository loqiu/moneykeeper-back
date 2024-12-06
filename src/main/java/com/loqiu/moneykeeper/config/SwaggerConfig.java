package com.loqiu.moneykeeper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(apiInfo());
    }


    private Info apiInfo() {
        return new Info()
                .title("MoneyKeeper API文档")
                .description("MoneyKeeper项目的API文档")
                .version("1.0")
                .contact(new Contact()
                    .name("Loqiu")
                    .email(""))
                .version("1.0");
    }
}