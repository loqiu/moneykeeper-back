package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.mapper.UserMapper;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private LedgerService ledgerService;

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

    @Override
    @Transactional
    public boolean save(User entity) {
        boolean saved = super.save(entity);
        if (saved && entity != null && entity.getId() != null) {
            ledgerService.getOrCreatePersonalLedger(entity.getId());
        }
        return saved;
    }
}
