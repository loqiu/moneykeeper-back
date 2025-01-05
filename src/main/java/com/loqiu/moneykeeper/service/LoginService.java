package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.vo.LoginResponse;

public interface LoginService {

    /**
     * 验证前端传来的 idToken
     * @param idToken 前端传来的 id_token
     * @return 如果验证成功，返回 GoogleIdToken.Payload；否则返回 null
     */
    public LoginResponse verifyGoogleIdToken(String idToken);

}
