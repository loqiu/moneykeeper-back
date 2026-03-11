package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerSummaryDTO {
    private Long id;
    private String name;
    private String type;
    private Long ownerUserId;
    private String memberRole;
    private boolean defaultLedger;
}
