package com.loqiu.moneykeeperback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.loqiu.moneykeeper.mapper")
@ComponentScan(basePackages = {"com.loqiu.moneykeeper"})
@EntityScan("com.loqiu.moneykeeper.entity")
public class MoneykeeperBackApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MoneykeeperBackApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MoneykeeperBackApplication.class);
    }
}
