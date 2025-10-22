package com.example.shopmark2.user.application.service;

import com.example.shopmark2.user.domain.model.User;
import com.example.shopmark2.user.domain.model.UserRole;
import com.example.shopmark2.user.domain.repository.UserRepository;
import com.example.shopmark2.user.presentation.advice.UserError;
import com.example.shopmark2.user.presentation.advice.UserException;
import com.example.shopmark2.user.presentation.dto.response.ResGetUsersDtoV1;
import com.example.shopmark2.user.presentation.dto.response.ResGetUsersWithIdDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV1 {

    private final UserRepository userRepository;

    public ResGetUsersDtoV1 getUsers(
            UUID authUserId,
            List<String> authUserRoleList,
            Pageable pageable,
            String username,
            String nickname,
            String email
    ) {
        String normalizedUsername = normalize(username);
        String normalizedNickname = normalize(nickname);
        String normalizedEmail = normalize(email);

        Page<User> userPage;
        if (isAdmin(authUserRoleList) || isManager(authUserRoleList)) {
            userPage = userRepository.searchUsers(normalizedUsername, normalizedNickname, normalizedEmail, pageable);
        } else {
            if (authUserId == null) {
                throw new UserException(UserError.USER_BAD_REQUEST);
            }
            User user = userRepository.findDefaultById(authUserId);
            if (!matchesFilter(user, normalizedUsername, normalizedNickname, normalizedEmail)) {
                userPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else if (pageable.getPageNumber() > 0) {
                userPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else {
                userPage = new PageImpl<>(List.of(user), pageable, 1);
            }
        }
        return ResGetUsersDtoV1.of(userPage);
    }

    public ResGetUsersWithIdDtoV1 getUsersWithId(UUID authUserId, List<String> authUserRoleList, UUID userId) {
        User user = userRepository.findDefaultById(userId);
        validateBy(authUserId, authUserRoleList, user);
        return ResGetUsersWithIdDtoV1.of(user);
    }

    public void deleteUsersById(UUID authUserId, List<String> authUserRoleList, UUID userId) {
        User user = userRepository.findDefaultById(userId);
        validateBy(authUserId, authUserRoleList, user);
        if (user.getUserRoleList().stream().map(UserRole::getRole).anyMatch(role -> role.equals(UserRole.Role.ADMIN))) {
            throw new UserException(UserError.USER_BAD_REQUEST);
        }
        User deletedUser = user.markDeleted(Instant.now(), authUserId);
        userRepository.save(deletedUser);
    }

    private void validateBy(UUID authUserId, List<String> authUserRoleList, User user) {
        if (
                user.getUserRoleList().stream().map(UserRole::getRole).anyMatch(role -> role.equals(UserRole.Role.ADMIN))
                        && !isAdmin(authUserRoleList)
        ) {
            throw new UserException(UserError.USER_BAD_REQUEST);
        }
        if (
                (authUserId != null && authUserId.equals(user.getId()))
                        || isAdmin(authUserRoleList)
                        || isManager(authUserRoleList)
        ) {
            return;
        }
        throw new UserException(UserError.USER_BAD_REQUEST);
    }

    private boolean isAdmin(List<String> authUserRoleList) {
        return authUserRoleList != null && authUserRoleList.contains(UserRole.Role.ADMIN.toString());
    }

    private boolean isManager(List<String> authUserRoleList) {
        return authUserRoleList != null && authUserRoleList.contains(UserRole.Role.MANAGER.toString());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean matchesFilter(User user, String username, String nickname, String email) {
        return matchesLike(user.getUsername(), username)
                && matchesLike(user.getNickname(), nickname)
                && matchesLike(user.getEmail(), email);
    }

    private boolean matchesLike(String target, String filter) {
        if (filter == null) {
            return true;
        }
        if (target == null) {
            return false;
        }
        return target.toLowerCase().contains(filter.toLowerCase());
    }
}
