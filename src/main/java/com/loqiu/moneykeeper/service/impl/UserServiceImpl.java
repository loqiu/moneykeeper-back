package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.mapper.UserMapper;
import com.loqiu.moneykeeper.service.UserService;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {



    @Override
    public User findByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    @Override
    public User findByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email must not be null");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return getOne(queryWrapper);
    }

    @Override
    public User findByUserPin(String userPin) {
        if (userPin == null) {
            throw new IllegalArgumentException("userPin must not be null");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_pin", userPin);
        return getOne(queryWrapper);
    }

}