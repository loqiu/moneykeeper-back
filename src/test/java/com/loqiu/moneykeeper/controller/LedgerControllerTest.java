package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.LedgerInviteDTO;
import com.loqiu.moneykeeper.dto.LedgerMemberDTO;
import com.loqiu.moneykeeper.dto.LedgerSummaryDTO;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LedgerControllerTest {

    @Mock
    private LedgerService ledgerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LedgerController controller = new LedgerController();
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createLedgerShouldReturnCreatedLedger() throws Exception {
        when(ledgerService.createLedger(1L, "Family Ledger", "family")).thenReturn(
                LedgerSummaryDTO.builder()
                        .id(31L)
                        .name("Family Ledger")
                        .type("family")
                        .ownerUserId(1L)
                        .memberRole("owner")
                        .defaultLedger(false)
                        .build()
        );

        mockMvc.perform(post("/api/ledgers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Family Ledger",
                                  "type": "family"
                                }
                                """)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ledger created"))
                .andExpect(jsonPath("$.data.id").value(31))
                .andExpect(jsonPath("$.data.type").value("family"));
    }

    @Test
    void listLedgersShouldReturnCurrentUsersLedgers() throws Exception {
        when(ledgerService.listLedgersForUser(1L)).thenReturn(List.of(
                LedgerSummaryDTO.builder()
                        .id(21L)
                        .name("Personal Ledger")
                        .type("personal")
                        .ownerUserId(1L)
                        .memberRole("owner")
                        .defaultLedger(true)
                        .build()
        ));

        mockMvc.perform(get("/api/ledgers")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(21))
                .andExpect(jsonPath("$.data[0].defaultLedger").value(true));
    }

    @Test
    void getDefaultLedgerShouldReturnCurrentUsersDefaultLedger() throws Exception {
        when(ledgerService.getDefaultLedgerSummary(1L)).thenReturn(
                LedgerSummaryDTO.builder()
                        .id(21L)
                        .name("Personal Ledger")
                        .type("personal")
                        .ownerUserId(1L)
                        .memberRole("owner")
                        .defaultLedger(true)
                        .build()
        );

        mockMvc.perform(get("/api/ledgers/default")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(21))
                .andExpect(jsonPath("$.data.name").value("Personal Ledger"));
    }

    @Test
    void listMembersShouldReturnLedgerMembers() throws Exception {
        when(ledgerService.listMembers(31L, 1L, false)).thenReturn(List.of(
                LedgerMemberDTO.builder()
                        .userId(1L)
                        .username("alice")
                        .email("alice@example.com")
                        .role("owner")
                        .status("active")
                        .joinedAt(LocalDateTime.of(2026, 3, 11, 9, 0))
                        .build()
        ));

        mockMvc.perform(get("/api/ledgers/31/members")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].role").value("owner"));
    }

    @Test
    void createInviteShouldReturnInviteDetails() throws Exception {
        when(ledgerService.createInvite(eq(31L), eq(1L), eq(false), eq("bob@example.com"), eq("member"), eq(7)))
                .thenReturn(LedgerInviteDTO.builder()
                        .id(7L)
                        .ledgerId(31L)
                        .ledgerName("Family Ledger")
                        .invitedByUserId(1L)
                        .invitedEmail("bob@example.com")
                        .inviteCode("invite-123")
                        .role("member")
                        .status("pending")
                        .build());

        mockMvc.perform(post("/api/ledgers/31/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invitedEmail": "bob@example.com",
                                  "role": "member",
                                  "expiresInDays": 7
                                }
                                """)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invite created"))
                .andExpect(jsonPath("$.data.invitedEmail").value("bob@example.com"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    void listPendingInvitesShouldReturnCurrentUsersInvites() throws Exception {
        when(ledgerService.listPendingInvitesForUser(2L)).thenReturn(List.of(
                LedgerInviteDTO.builder()
                        .id(9L)
                        .ledgerId(31L)
                        .ledgerName("Family Ledger")
                        .invitedByUserId(1L)
                        .invitedEmail("bob@example.com")
                        .inviteCode("invite-123")
                        .role("member")
                        .status("pending")
                        .build()
        ));

        mockMvc.perform(get("/api/ledgers/invites")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].ledgerName").value("Family Ledger"))
                .andExpect(jsonPath("$.data[0].inviteCode").value("invite-123"));
    }

    @Test
    void acceptInviteShouldReturnJoinedLedger() throws Exception {
        when(ledgerService.acceptInvite("invite-123", 2L)).thenReturn(
                LedgerSummaryDTO.builder()
                        .id(31L)
                        .name("Family Ledger")
                        .type("family")
                        .ownerUserId(1L)
                        .memberRole("member")
                        .defaultLedger(false)
                        .build()
        );

        mockMvc.perform(post("/api/ledgers/invites/invite-123/accept")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invite accepted"))
                .andExpect(jsonPath("$.data.id").value(31))
                .andExpect(jsonPath("$.data.memberRole").value("member"));
    }
}
