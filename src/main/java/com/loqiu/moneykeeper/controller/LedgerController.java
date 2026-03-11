package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.LedgerInviteDTO;
import com.loqiu.moneykeeper.dto.LedgerMemberDTO;
import com.loqiu.moneykeeper.dto.LedgerSummaryDTO;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.LedgerCreateRequest;
import com.loqiu.moneykeeper.vo.LedgerInviteRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {

    @Autowired
    private LedgerService ledgerService;

    @PostMapping
    public MkApiResponse<LedgerSummaryDTO> createLedger(@RequestBody(required = false) LedgerCreateRequest createRequest,
                                                        HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        LedgerSummaryDTO ledger = ledgerService.createLedger(
                currentUserId,
                createRequest == null ? null : createRequest.getName(),
                createRequest == null ? null : createRequest.getType()
        );
        return MkApiResponse.success("Ledger created", ledger);
    }

    @GetMapping
    public MkApiResponse<List<LedgerSummaryDTO>> listLedgers(HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return MkApiResponse.success(ledgerService.listLedgersForUser(currentUserId));
    }

    @GetMapping("/default")
    public MkApiResponse<LedgerSummaryDTO> getDefaultLedger(HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return MkApiResponse.success(ledgerService.getDefaultLedgerSummary(currentUserId));
    }

    @GetMapping("/{ledgerId}/members")
    public MkApiResponse<List<LedgerMemberDTO>> listMembers(@PathVariable Long ledgerId, HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return MkApiResponse.success(ledgerService.listMembers(ledgerId, currentUserId, RequestAuthUtil.isAdmin(request)));
    }

    @PostMapping("/{ledgerId}/invites")
    public MkApiResponse<LedgerInviteDTO> createInvite(@PathVariable Long ledgerId,
                                                       @RequestBody(required = false) LedgerInviteRequest inviteRequest,
                                                       HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        LedgerInviteDTO invite = ledgerService.createInvite(
                ledgerId,
                currentUserId,
                RequestAuthUtil.isAdmin(request),
                inviteRequest == null ? null : inviteRequest.getInvitedEmail(),
                inviteRequest == null ? null : inviteRequest.getRole(),
                inviteRequest == null ? null : inviteRequest.getExpiresInDays()
        );
        return MkApiResponse.success("Invite created", invite);
    }

    @GetMapping("/{ledgerId}/invites")
    public MkApiResponse<List<LedgerInviteDTO>> listLedgerInvites(@PathVariable Long ledgerId, HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return MkApiResponse.success(ledgerService.listInvites(ledgerId, currentUserId, RequestAuthUtil.isAdmin(request)));
    }

    @GetMapping("/invites")
    public MkApiResponse<List<LedgerInviteDTO>> listPendingInvites(HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return MkApiResponse.success(ledgerService.listPendingInvitesForUser(currentUserId));
    }

    @PostMapping("/invites/{inviteCode}/accept")
    public MkApiResponse<LedgerSummaryDTO> acceptInvite(@PathVariable String inviteCode, HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        LedgerSummaryDTO ledger = ledgerService.acceptInvite(inviteCode, currentUserId);
        return MkApiResponse.success("Invite accepted", ledger);
    }
}
