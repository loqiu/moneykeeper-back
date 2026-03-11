package com.loqiu.moneykeeper.vo;

import lombok.Data;

@Data
public class LedgerInviteRequest {
    private String invitedEmail;
    private String role;
    private Integer expiresInDays;
}
