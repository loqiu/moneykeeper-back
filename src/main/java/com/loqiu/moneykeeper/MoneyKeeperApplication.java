package com.loqiu.moneykeeper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.moneykeeper.mapper")
public class MoneyKeeperApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoneyKeeperApplication.class, args);
    }
} 