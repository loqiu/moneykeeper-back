package com.loqiu.moneykeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.loqiu.moneykeeper.entity.User;

public interface UserService extends IService<User> {
    // 可以添加自定义的业务方法
    User findByUsername(String username);
} 