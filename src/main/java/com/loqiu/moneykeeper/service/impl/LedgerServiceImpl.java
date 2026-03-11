package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loqiu.moneykeeper.dto.LedgerInviteDTO;
import com.loqiu.moneykeeper.dto.LedgerMemberDTO;
import com.loqiu.moneykeeper.dto.LedgerSummaryDTO;
import com.loqiu.moneykeeper.entity.Ledger;
import com.loqiu.moneykeeper.entity.LedgerInvite;
import com.loqiu.moneykeeper.entity.LedgerMember;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ConflictException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.mapper.LedgerInviteMapper;
import com.loqiu.moneykeeper.mapper.LedgerMapper;
import com.loqiu.moneykeeper.mapper.LedgerMemberMapper;
import com.loqiu.moneykeeper.mapper.UserMapper;
import com.loqiu.moneykeeper.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LedgerServiceImpl extends ServiceImpl<LedgerMapper, Ledger> implements LedgerService {

    private static final String LEDGER_TYPE_PERSONAL = "personal";
    private static final String LEDGER_TYPE_SHARED = "shared";
    private static final Set<String> SHAREABLE_LEDGER_TYPES = Set.of(LEDGER_TYPE_SHARED, "family", "project");

    private static final String LEDGER_MEMBER_ROLE_OWNER = "owner";
    private static final String LEDGER_MEMBER_ROLE_ADMIN = "admin";
    private static final String LEDGER_MEMBER_ROLE_MEMBER = "member";
    private static final Set<String> INVITABLE_MEMBER_ROLES = Set.of(LEDGER_MEMBER_ROLE_ADMIN, LEDGER_MEMBER_ROLE_MEMBER);

    private static final String LEDGER_MEMBER_STATUS_ACTIVE = "active";

    private static final String LEDGER_INVITE_STATUS_PENDING = "pending";
    private static final String LEDGER_INVITE_STATUS_ACCEPTED = "accepted";
    private static final String LEDGER_INVITE_STATUS_EXPIRED = "expired";

    private static final int DEFAULT_INVITE_EXPIRES_IN_DAYS = 7;
    private static final int MAX_INVITE_EXPIRES_IN_DAYS = 30;

    @Autowired
    private LedgerMemberMapper ledgerMemberMapper;

    @Autowired
    private LedgerInviteMapper ledgerInviteMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Ledger getOrCreatePersonalLedger(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }

        Ledger existingLedger = findDefaultLedgerByUserId(userId);
        if (existingLedger != null) {
            upsertMembership(existingLedger.getId(), userId, LEDGER_MEMBER_ROLE_OWNER, LEDGER_MEMBER_STATUS_ACTIVE);
            return existingLedger;
        }

        Ledger ledger = Ledger.builder()
                .name("Personal Ledger")
                .type(LEDGER_TYPE_PERSONAL)
                .ownerUserId(userId)
                .isDefault(1)
                .build();
        save(ledger);
        upsertMembership(ledger.getId(), userId, LEDGER_MEMBER_ROLE_OWNER, LEDGER_MEMBER_STATUS_ACTIVE);
        return ledger;
    }

    @Override
    public Ledger findDefaultLedgerByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        QueryWrapper<Ledger> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("owner_user_id", userId)
                .eq("type", LEDGER_TYPE_PERSONAL)
                .eq("is_default", 1)
                .eq("deleted_at", 0)
                .last("limit 1");
        return getOne(queryWrapper, false);
    }

    @Override
    @Transactional
    public LedgerSummaryDTO createLedger(Long ownerUserId, String name, String type) {
        if (ownerUserId == null) {
            throw new BadRequestException("User id is required");
        }
        String normalizedName = normalizeRequiredText(name, "Ledger name is required");
        String normalizedType = normalizeLedgerType(type);

        Ledger ledger = Ledger.builder()
                .name(normalizedName)
                .type(normalizedType)
                .ownerUserId(ownerUserId)
                .isDefault(0)
                .build();
        save(ledger);
        upsertMembership(ledger.getId(), ownerUserId, LEDGER_MEMBER_ROLE_OWNER, LEDGER_MEMBER_STATUS_ACTIVE);
        return toSummary(ledger, LEDGER_MEMBER_ROLE_OWNER);
    }

    @Override
    public List<LedgerSummaryDTO> listLedgersForUser(Long userId) {
        QueryWrapper<LedgerMember> membershipQuery = new QueryWrapper<>();
        membershipQuery.eq("user_id", userId)
                .eq("status", LEDGER_MEMBER_STATUS_ACTIVE)
                .orderByDesc("joined_at", "id");

        List<LedgerMember> memberships = ledgerMemberMapper.selectList(membershipQuery);
        if (memberships == null || memberships.isEmpty()) {
            Ledger personalLedger = getOrCreatePersonalLedger(userId);
            return List.of(toSummary(personalLedger, LEDGER_MEMBER_ROLE_OWNER));
        }

        Map<Long, LedgerMember> membershipByLedgerId = new LinkedHashMap<>();
        for (LedgerMember membership : memberships) {
            membershipByLedgerId.putIfAbsent(membership.getLedgerId(), membership);
        }

        List<Ledger> ledgers = listByIds(membershipByLedgerId.keySet()).stream()
                .filter(this::isActiveLedger)
                .sorted(Comparator
                        .comparing((Ledger ledger) -> ledger.getIsDefault() != null && ledger.getIsDefault() == 1)
                        .reversed()
                        .thenComparing(Ledger::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();

        if (ledgers.isEmpty()) {
            Ledger personalLedger = getOrCreatePersonalLedger(userId);
            return List.of(toSummary(personalLedger, LEDGER_MEMBER_ROLE_OWNER));
        }

        return ledgers.stream()
                .map(ledger -> toSummary(ledger, membershipByLedgerId.get(ledger.getId()) == null ? null : membershipByLedgerId.get(ledger.getId()).getRole()))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public LedgerSummaryDTO getDefaultLedgerSummary(Long userId) {
        Ledger ledger = getOrCreatePersonalLedger(userId);
        LedgerMember membership = findMembership(ledger.getId(), userId);
        return toSummary(ledger, membership == null ? LEDGER_MEMBER_ROLE_OWNER : membership.getRole());
    }

    @Override
    public Ledger requireLedger(Long ledgerId) {
        if (ledgerId == null) {
            throw new BadRequestException("Ledger id is required");
        }
        Ledger ledger = getById(ledgerId);
        if (!isActiveLedger(ledger)) {
            throw new ResourceNotFoundException("Ledger not found");
        }
        return ledger;
    }

    @Override
    public boolean hasActiveMembership(Long ledgerId, Long userId) {
        LedgerMember membership = findMembership(ledgerId, userId);
        return membership != null && LEDGER_MEMBER_STATUS_ACTIVE.equalsIgnoreCase(membership.getStatus());
    }

    @Override
    public boolean hasManagementPermission(Long ledgerId, Long userId) {
        LedgerMember membership = findMembership(ledgerId, userId);
        if (membership == null || !LEDGER_MEMBER_STATUS_ACTIVE.equalsIgnoreCase(membership.getStatus())) {
            return false;
        }
        return LEDGER_MEMBER_ROLE_OWNER.equalsIgnoreCase(membership.getRole())
                || LEDGER_MEMBER_ROLE_ADMIN.equalsIgnoreCase(membership.getRole());
    }

    @Override
    public List<LedgerMemberDTO> listMembers(Long ledgerId, Long currentUserId, boolean platformAdmin) {
        Ledger ledger = requireLedger(ledgerId);
        if (!platformAdmin && !hasActiveMembership(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to view this ledger's members");
        }

        QueryWrapper<LedgerMember> membershipQuery = new QueryWrapper<>();
        membershipQuery.eq("ledger_id", ledgerId)
                .eq("status", LEDGER_MEMBER_STATUS_ACTIVE)
                .orderByAsc("joined_at", "id");
        List<LedgerMember> memberships = ledgerMemberMapper.selectList(membershipQuery);
        if (memberships == null || memberships.isEmpty()) {
            return List.of();
        }

        Map<Long, User> usersById = userMapper.selectBatchIds(
                        memberships.stream().map(LedgerMember::getUserId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));

        return memberships.stream()
                .sorted(Comparator
                        .comparingInt((LedgerMember membership) -> roleOrder(membership.getRole()))
                        .thenComparing(LedgerMember::getJoinedAt, Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(LedgerMember::getUserId, Comparator.nullsLast(Long::compareTo)))
                .map(membership -> toMemberDto(membership, usersById.get(membership.getUserId()), ledger))
                .toList();
    }

    @Override
    public List<LedgerInviteDTO> listInvites(Long ledgerId, Long currentUserId, boolean platformAdmin) {
        Ledger ledger = requireLedger(ledgerId);
        if (!platformAdmin && !hasManagementPermission(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to view this ledger's invites");
        }

        QueryWrapper<LedgerInvite> inviteQuery = new QueryWrapper<>();
        inviteQuery.eq("ledger_id", ledgerId)
                .orderByDesc("created_at", "id");

        List<LedgerInvite> invites = ledgerInviteMapper.selectList(inviteQuery);
        if (invites == null || invites.isEmpty()) {
            return List.of();
        }

        return invites.stream()
                .map(this::refreshInviteStatus)
                .map(invite -> toInviteDto(invite, ledger))
                .toList();
    }

    @Override
    public List<LedgerInviteDTO> listPendingInvitesForUser(Long currentUserId) {
        if (currentUserId == null) {
            throw new BadRequestException("User id is required");
        }

        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !StringUtils.hasText(currentUser.getEmail())) {
            return List.of();
        }

        String normalizedEmail = normalizeEmail(currentUser.getEmail());
        QueryWrapper<LedgerInvite> inviteQuery = new QueryWrapper<>();
        inviteQuery.eq("invited_email", normalizedEmail)
                .eq("status", LEDGER_INVITE_STATUS_PENDING)
                .orderByDesc("created_at", "id");

        List<LedgerInvite> pendingInvites = ledgerInviteMapper.selectList(inviteQuery).stream()
                .map(this::refreshInviteStatus)
                .filter(invite -> LEDGER_INVITE_STATUS_PENDING.equalsIgnoreCase(invite.getStatus()))
                .toList();
        if (pendingInvites.isEmpty()) {
            return List.of();
        }

        Map<Long, Ledger> ledgersById = listByIds(
                        pendingInvites.stream().map(LedgerInvite::getLedgerId).distinct().toList()
                ).stream()
                .filter(this::isActiveLedger)
                .collect(Collectors.toMap(Ledger::getId, Function.identity(), (left, right) -> left));

        return pendingInvites.stream()
                .map(invite -> toInviteDto(invite, ledgersById.get(invite.getLedgerId())))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public LedgerInviteDTO createInvite(Long ledgerId,
                                        Long currentUserId,
                                        boolean platformAdmin,
                                        String invitedEmail,
                                        String role,
                                        Integer expiresInDays) {
        Ledger ledger = requireLedger(ledgerId);
        if (LEDGER_TYPE_PERSONAL.equalsIgnoreCase(ledger.getType())) {
            throw new BadRequestException("Personal ledgers cannot be shared");
        }
        if (!platformAdmin && !hasManagementPermission(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to invite members to this ledger");
        }

        String normalizedEmail = normalizeEmail(invitedEmail);
        String normalizedRole = normalizeInviteRole(role);
        int validExpiresInDays = resolveExpiresInDays(expiresInDays);

        User invitedUser = findUserByEmail(normalizedEmail);
        if (invitedUser != null && hasActiveMembership(ledgerId, invitedUser.getId())) {
            throw new ConflictException("User is already a member of this ledger");
        }

        QueryWrapper<LedgerInvite> inviteQuery = new QueryWrapper<>();
        inviteQuery.eq("ledger_id", ledgerId)
                .eq("invited_email", normalizedEmail)
                .eq("status", LEDGER_INVITE_STATUS_PENDING)
                .orderByDesc("created_at", "id");
        List<LedgerInvite> existingInvites = ledgerInviteMapper.selectList(inviteQuery);
        for (LedgerInvite existingInvite : existingInvites) {
            LedgerInvite refreshedInvite = refreshInviteStatus(existingInvite);
            if (LEDGER_INVITE_STATUS_PENDING.equalsIgnoreCase(refreshedInvite.getStatus())) {
                return toInviteDto(refreshedInvite, ledger);
            }
        }

        LedgerInvite invite = LedgerInvite.builder()
                .ledgerId(ledgerId)
                .invitedByUserId(currentUserId)
                .invitedEmail(normalizedEmail)
                .inviteCode(generateInviteCode())
                .role(normalizedRole)
                .status(LEDGER_INVITE_STATUS_PENDING)
                .expiresAt(LocalDateTime.now().plusDays(validExpiresInDays))
                .build();
        ledgerInviteMapper.insert(invite);
        return toInviteDto(invite, ledger);
    }

    @Override
    @Transactional
    public LedgerSummaryDTO acceptInvite(String inviteCode, Long currentUserId) {
        if (!StringUtils.hasText(inviteCode)) {
            throw new BadRequestException("Invite code is required");
        }
        if (currentUserId == null) {
            throw new BadRequestException("User id is required");
        }

        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not found");
        }
        if (!StringUtils.hasText(currentUser.getEmail())) {
            throw new BadRequestException("Current user does not have an email address");
        }

        QueryWrapper<LedgerInvite> inviteQuery = new QueryWrapper<>();
        inviteQuery.eq("invite_code", inviteCode.trim())
                .last("limit 1");
        LedgerInvite invite = ledgerInviteMapper.selectOne(inviteQuery);
        if (invite == null) {
            throw new ResourceNotFoundException("Invite not found");
        }
        invite = refreshInviteStatus(invite);
        if (!LEDGER_INVITE_STATUS_PENDING.equalsIgnoreCase(invite.getStatus())) {
            throw new BadRequestException("Invite is no longer available");
        }

        String currentUserEmail = normalizeEmail(currentUser.getEmail());
        if (!currentUserEmail.equalsIgnoreCase(normalizeEmail(invite.getInvitedEmail()))) {
            throw new ForbiddenException("You do not have permission to accept this invite");
        }

        Ledger ledger = requireLedger(invite.getLedgerId());
        upsertMembership(ledger.getId(), currentUserId, invite.getRole(), LEDGER_MEMBER_STATUS_ACTIVE);

        invite.setStatus(LEDGER_INVITE_STATUS_ACCEPTED);
        invite.setAcceptedAt(LocalDateTime.now());
        ledgerInviteMapper.updateById(invite);
        return toSummary(ledger, resolveMembershipRole(ledger.getId(), currentUserId));
    }

    private boolean isActiveLedger(Ledger ledger) {
        return ledger != null && (ledger.getDeletedAt() == null || ledger.getDeletedAt() == 0);
    }

    private LedgerMember findMembership(Long ledgerId, Long userId) {
        if (ledgerId == null || userId == null) {
            return null;
        }
        QueryWrapper<LedgerMember> membershipQuery = new QueryWrapper<>();
        membershipQuery.eq("ledger_id", ledgerId)
                .eq("user_id", userId)
                .last("limit 1");
        return ledgerMemberMapper.selectOne(membershipQuery);
    }

    private void upsertMembership(Long ledgerId, Long userId, String role, String status) {
        LedgerMember membership = findMembership(ledgerId, userId);
        if (membership == null) {
            ledgerMemberMapper.insert(LedgerMember.builder()
                    .ledgerId(ledgerId)
                    .userId(userId)
                    .role(role)
                    .status(status)
                    .joinedAt(LocalDateTime.now())
                    .build());
            return;
        }

        membership.setRole(role);
        membership.setStatus(status);
        if (membership.getJoinedAt() == null && LEDGER_MEMBER_STATUS_ACTIVE.equalsIgnoreCase(status)) {
            membership.setJoinedAt(LocalDateTime.now());
        }
        ledgerMemberMapper.updateById(membership);
    }

    private String resolveMembershipRole(Long ledgerId, Long userId) {
        LedgerMember membership = findMembership(ledgerId, userId);
        return membership == null ? null : membership.getRole();
    }

    private LedgerInvite refreshInviteStatus(LedgerInvite invite) {
        if (invite == null) {
            return null;
        }
        if (LEDGER_INVITE_STATUS_PENDING.equalsIgnoreCase(invite.getStatus())
                && invite.getExpiresAt() != null
                && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(LEDGER_INVITE_STATUS_EXPIRED);
            ledgerInviteMapper.updateById(invite);
        }
        return invite;
    }

    private LedgerSummaryDTO toSummary(Ledger ledger, String memberRole) {
        if (ledger == null) {
            return null;
        }
        return LedgerSummaryDTO.builder()
                .id(ledger.getId())
                .name(ledger.getName())
                .type(ledger.getType())
                .ownerUserId(ledger.getOwnerUserId())
                .memberRole(memberRole)
                .defaultLedger(ledger.getIsDefault() != null && ledger.getIsDefault() == 1)
                .build();
    }

    private LedgerMemberDTO toMemberDto(LedgerMember membership, User user, Ledger ledger) {
        if (membership == null || ledger == null) {
            return null;
        }
        return LedgerMemberDTO.builder()
                .userId(membership.getUserId())
                .username(user == null ? null : user.getUsername())
                .email(user == null ? null : user.getEmail())
                .role(membership.getRole())
                .status(membership.getStatus())
                .joinedAt(membership.getJoinedAt())
                .build();
    }

    private LedgerInviteDTO toInviteDto(LedgerInvite invite, Ledger ledger) {
        if (invite == null || ledger == null) {
            return null;
        }
        return LedgerInviteDTO.builder()
                .id(invite.getId())
                .ledgerId(invite.getLedgerId())
                .ledgerName(ledger.getName())
                .invitedByUserId(invite.getInvitedByUserId())
                .invitedEmail(invite.getInvitedEmail())
                .inviteCode(invite.getInviteCode())
                .role(invite.getRole())
                .status(invite.getStatus())
                .expiresAt(invite.getExpiresAt())
                .acceptedAt(invite.getAcceptedAt())
                .createdAt(invite.getCreatedAt())
                .build();
    }

    private User findUserByEmail(String email) {
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("email", email)
                .last("limit 1");
        return userMapper.selectOne(userQuery);
    }

    private String normalizeRequiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String normalizeLedgerType(String type) {
        if (!StringUtils.hasText(type)) {
            return LEDGER_TYPE_SHARED;
        }
        String normalizedType = type.trim().toLowerCase();
        if (LEDGER_TYPE_PERSONAL.equals(normalizedType)) {
            throw new BadRequestException("Use the personal-ledger flow instead of creating one manually");
        }
        if (!SHAREABLE_LEDGER_TYPES.contains(normalizedType)) {
            throw new BadRequestException("Ledger type must be one of: shared, family, project");
        }
        return normalizedType;
    }

    private String normalizeInviteRole(String role) {
        if (!StringUtils.hasText(role)) {
            return LEDGER_MEMBER_ROLE_MEMBER;
        }
        String normalizedRole = role.trim().toLowerCase();
        if (!INVITABLE_MEMBER_ROLES.contains(normalizedRole)) {
            throw new BadRequestException("Invite role must be one of: admin, member");
        }
        return normalizedRole;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BadRequestException("Invite email is required");
        }
        return email.trim().toLowerCase();
    }

    private int resolveExpiresInDays(Integer expiresInDays) {
        if (expiresInDays == null) {
            return DEFAULT_INVITE_EXPIRES_IN_DAYS;
        }
        if (expiresInDays < 1 || expiresInDays > MAX_INVITE_EXPIRES_IN_DAYS) {
            throw new BadRequestException("Invite expiry must be between 1 and 30 days");
        }
        return expiresInDays;
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private int roleOrder(String role) {
        if (LEDGER_MEMBER_ROLE_OWNER.equalsIgnoreCase(role)) {
            return 0;
        }
        if (LEDGER_MEMBER_ROLE_ADMIN.equalsIgnoreCase(role)) {
            return 1;
        }
        return 2;
    }
}
