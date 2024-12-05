package com.loqiu.moneykeeper;

import com.loqiu.moneykeeper.mapper.UserMapper;
import com.loqiu.moneykeeper.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.loqiu.moneykeeperback.MoneykeeperBackApplication;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@SpringBootTest(classes = MoneykeeperBackApplication.class)
public class DatabaseTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testDatabaseConnection() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("数据库连接成功！");
            System.out.println("数据库URL: " + conn.getMetaData().getURL());
            System.out.println("数据库用户名: " + conn.getMetaData().getUserName());
            System.out.println("数据库产品名称: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("数据库版本: " + conn.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testMyBatisPlus() {
        try {
            // 测试查询所有用户
            List<User> users = userMapper.selectList(null);
            System.out.println("查询成功！总用户数: " + users.size());
            users.forEach(user -> System.out.println("用户信息: " + user));
        } catch (Exception e) {
            System.err.println("MyBatis-Plus测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 