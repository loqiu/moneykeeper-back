package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerMemberDTO {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String status;
    private LocalDateTime joinedAt;
}
