package com.example.shopmark2.user.domain.repository;

import com.example.shopmark2.global.infrastructure.config.jpa.JpaAuditConfig;
import com.example.shopmark2.global.infrastructure.config.jpa.QuerydslConfig;
import com.example.shopmark2.global.infrastructure.config.jpa.audit.CustomAuditAware;
import com.example.shopmark2.user.domain.model.User;
import com.example.shopmark2.user.domain.model.UserRole;
import com.example.shopmark2.user.presentation.advice.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaAuditConfig.class, CustomAuditAware.class, QuerydslConfig.class,
        com.example.shopmark2.user.infrastructure.persistence.repository.UserRepositoryImpl.class,
        com.example.shopmark2.user.infrastructure.persistence.mapper.UserMapper.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findDefaultById는 존재하는 사용자를 반환한다")
    void findDefaultByIdReturnsUser() {
        User savedUser = userRepository.save(createUser("tester1"));

        User foundUser = userRepository.findDefaultById(savedUser.getId());

        assertThat(foundUser.getUsername()).isEqualTo("tester1");
        assertThat(foundUser.getUserRoleList()).extracting(UserRole::getRole)
                .containsExactly(UserRole.Role.USER);
    }

    @Test
    @DisplayName("findDefaultById는 존재하지 않으면 예외를 던진다")
    void findDefaultByIdThrowsWhenMissing() {
        UUID randomId = UUID.randomUUID();

        assertThatThrownBy(() -> userRepository.findDefaultById(randomId))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("유저를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("findByUsername은 사용자명을 기준으로 조회한다")
    void findByUsernameReturnsOptional() {
        User savedUser = userRepository.save(createUser("tester2"));

        Optional<User> optionalUser = userRepository.findByUsername("tester2");

        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("searchUsers는 username, nickname, email LIKE 조건으로 필터링한다")
    void searchUsersFiltersByLikeConditions() {
        User matchedUser = userRepository.save(createUser("search-target"));
        userRepository.save(createUser("other-user"));

        Page<User> page = userRepository.searchUsers(
                "search",
                "nick",
                "example.com",
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(matchedUser.getId());
    }

    private User createUser(String username) {
        return User.builder()
                .username(username)
                .password("password123!")
                .nickname("nick-" + username)
                .email(username + "@example.com")
                .jwtValidator(0L)
                .userRoleList(List.of(UserRole.builder()
                        .id(null)
                        .role(UserRole.Role.USER)
                        .build()))
                .userSocialList(List.of())
                .build();
    }
}
