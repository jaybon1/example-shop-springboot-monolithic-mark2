package com.example.shop.user.application.service;

import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import com.example.shop.user.presentation.advice.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceV1Test {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceV1 userServiceV1;

    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        normalUser = createUser(UUID.randomUUID(), "normal-user", UserRole.Role.USER);
        adminUser = createUser(UUID.randomUUID(), "admin-user", UserRole.Role.ADMIN);
    }

    @Test
    @DisplayName("관리자는 사용자 목록 전체를 조회할 수 있다")
    void getUsersAsAdminReturnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.searchUsers(null, null, null, pageable)).thenReturn(new PageImpl<>(List.of(normalUser, adminUser)));

        var response = userServiceV1.getUsers(
                adminUser.getId(),
                List.of(UserRole.Role.ADMIN.toString()),
                pageable,
                null,
                null,
                null
        );

        assertThat(response.getUserPage().getContent()).hasSize(2);
        verify(userRepository).searchUsers(null, null, null, pageable);
    }

    @Test
    @DisplayName("관리자는 검색 조건으로 사용자 목록을 LIKE 조회할 수 있다")
    void getUsersAsAdminWithSearchFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.searchUsers("normal", "nickname", "example.com", pageable))
                .thenReturn(new PageImpl<>(List.of(normalUser), pageable, 1));

        var response = userServiceV1.getUsers(
                adminUser.getId(),
                List.of(UserRole.Role.ADMIN.toString()),
                pageable,
                " normal ",
                " nickname ",
                " example.com "
        );

        assertThat(response.getUserPage().getContent()).hasSize(1);
        verify(userRepository).searchUsers("normal", "nickname", "example.com", pageable);
    }

    @Test
    @DisplayName("사용자는 페이징 조회가 가능하다")
    void getUsersAsNormalUser() {
        Pageable firstPage = PageRequest.of(0, 10);
        when(userRepository.findDefaultById(normalUser.getId())).thenReturn(normalUser);

        var firstPageResponse = userServiceV1.getUsers(
                normalUser.getId(),
                List.of(UserRole.Role.USER.toString()),
                firstPage,
                null,
                null,
                null
        );

        assertThat(firstPageResponse.getUserPage().getContent()).hasSize(1);
        assertThat(firstPageResponse.getUserPage().getContent().get(0).getUsername()).isEqualTo("normal-user");

        Pageable secondPage = PageRequest.of(1, 10);
        var secondPageResponse = userServiceV1.getUsers(
                normalUser.getId(),
                List.of(UserRole.Role.USER.toString()),
                secondPage,
                null,
                null,
                null
        );

        assertThat(secondPageResponse.getUserPage().getContent()).isEmpty();
        verify(userRepository, times(2)).findDefaultById(normalUser.getId());
    }

    @Test
    @DisplayName("인증 사용자 정보가 없으면 목록 조회 시 예외를 던진다")
    void getUsersWithoutAuthUserIdThrows() {
        Pageable pageable = PageRequest.of(0, 5);

        assertThatThrownBy(() -> userServiceV1.getUsers(null, List.of(UserRole.Role.USER.toString()), pageable, null, null, null))
                .isInstanceOf(UserException.class);
    }

    @Test
    @DisplayName("일반 사용자는 검색 조건이 일치하지 않으면 빈 페이지가 반환된다")
    void getUsersAsNormalUserWithMismatchedFilterReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findDefaultById(normalUser.getId())).thenReturn(normalUser);

        var response = userServiceV1.getUsers(
                normalUser.getId(),
                List.of(UserRole.Role.USER.toString()),
                pageable,
                "another",
                null,
                null
        );

        assertThat(response.getUserPage().getContent()).isEmpty();
        verify(userRepository).findDefaultById(normalUser.getId());
    }

    @Test
    @DisplayName("동일 사용자는 상세 조회가 가능하다")
    void getUserAsSelf() {
        when(userRepository.findDefaultById(normalUser.getId())).thenReturn(normalUser);

        var response = userServiceV1.getUser(normalUser.getId(), List.of(UserRole.Role.USER.toString()), normalUser.getId());

        assertThat(response.getUser().getId()).isEqualTo(normalUser.getId().toString());
        verify(userRepository).findDefaultById(normalUser.getId());
    }

    @Test
    @DisplayName("관리자 정보는 관리자만 조회할 수 있다")
    void getUserAdminRequiresAdminRole() {
        when(userRepository.findDefaultById(adminUser.getId())).thenReturn(adminUser);

        assertThatThrownBy(() -> userServiceV1.getUser(normalUser.getId(), List.of(UserRole.Role.USER.toString()), adminUser.getId()))
                .isInstanceOf(UserException.class);
    }

    @Test
    @DisplayName("매니저는 다른 사용자를 삭제할 수 있으며, 삭제 정보가 기록된다")
    void deleteUserAsManager() {
        when(userRepository.findDefaultById(normalUser.getId())).thenReturn(normalUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userServiceV1.deleteUser(UUID.randomUUID(), List.of(UserRole.Role.MANAGER.toString()), normalUser.getId());

        verify(userRepository).save(argThat(savedUser -> savedUser.getDeletedAt() != null));
    }

    @Test
    @DisplayName("관리자를 삭제하려고 하면 예외를 던진다")
    void deleteAdminUserThrows() {
        when(userRepository.findDefaultById(adminUser.getId())).thenReturn(adminUser);

        assertThatThrownBy(() -> userServiceV1.deleteUser(UUID.randomUUID(), List.of(UserRole.Role.MANAGER.toString()), adminUser.getId()))
                .isInstanceOf(UserException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("권한이 없는 사용자는 다른 사용자를 조회할 수 없다")
    void getUserForbiddenForOtherUser() {
        when(userRepository.findDefaultById(normalUser.getId())).thenReturn(normalUser);

        assertThatThrownBy(() -> userServiceV1.getUser(UUID.randomUUID(), List.of(UserRole.Role.USER.toString()), normalUser.getId()))
                .isInstanceOf(UserException.class);
    }

    private User createUser(UUID id, String username, UserRole.Role primaryRole) {
        return User.builder()
                .id(id)
                .username(username)
                .password("secure-password")
                .nickname("nickname-" + username)
                .email(username + "@example.com")
                .jwtValidator(0L)
                .userRoleList(List.of(UserRole.builder()
                        .id(UUID.randomUUID())
                        .role(primaryRole)
                        .build()))
                .userSocialList(List.of())
                .build();
    }
}
