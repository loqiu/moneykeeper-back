package com.loqiu.moneykeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.loqiu.moneykeeper.dto.LedgerInviteDTO;
import com.loqiu.moneykeeper.dto.LedgerMemberDTO;
import com.loqiu.moneykeeper.dto.LedgerSummaryDTO;
import com.loqiu.moneykeeper.entity.Ledger;

import java.util.List;

public interface LedgerService extends IService<Ledger> {
    Ledger getOrCreatePersonalLedger(Long userId);

    Ledger findDefaultLedgerByUserId(Long userId);

    LedgerSummaryDTO createLedger(Long ownerUserId, String name, String type);

    List<LedgerSummaryDTO> listLedgersForUser(Long userId);

    LedgerSummaryDTO getDefaultLedgerSummary(Long userId);

    Ledger requireLedger(Long ledgerId);

    boolean hasActiveMembership(Long ledgerId, Long userId);

    boolean hasManagementPermission(Long ledgerId, Long userId);

    List<LedgerMemberDTO> listMembers(Long ledgerId, Long currentUserId, boolean platformAdmin);

    List<LedgerInviteDTO> listInvites(Long ledgerId, Long currentUserId, boolean platformAdmin);

    List<LedgerInviteDTO> listPendingInvitesForUser(Long currentUserId);

    LedgerInviteDTO createInvite(Long ledgerId,
                                 Long currentUserId,
                                 boolean platformAdmin,
                                 String invitedEmail,
                                 String role,
                                 Integer expiresInDays);

    LedgerSummaryDTO acceptInvite(String inviteCode, Long currentUserId);
}
