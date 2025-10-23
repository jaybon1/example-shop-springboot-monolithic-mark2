package com.example.shop.user.domain.repository;

import com.example.shop.user.domain.model.User;
import com.example.shop.user.presentation.advice.UserError;
import com.example.shop.user.presentation.advice.UserException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID userId);

    Optional<User> findByUsername(String username);

    Page<User> searchUsers(String username, String nickname, String email, Pageable pageable);

    long count();

    default User findDefaultById(UUID userId) {
        return findById(userId).orElseThrow(() -> new UserException(UserError.USER_CAN_NOT_FOUND));
    }
}
