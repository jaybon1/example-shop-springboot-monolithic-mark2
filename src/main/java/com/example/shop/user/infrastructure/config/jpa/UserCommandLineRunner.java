package com.example.shop.user.infrastructure.config.jpa;

import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class UserCommandLineRunner implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            saveUserEntityBy(1);
            saveUserEntityBy(2);
            saveUserEntityBy(3);
        }
    }

    private void saveUserEntityBy(Integer number) {
        User user = User.builder()
                .username("temp" + number)
                .password(passwordEncoder.encode("temp1234"))
                .nickname("temp" + number)
                .email("temp%d@temp.com".formatted(number))
                .jwtValidator(0L)
                .userRoleList(List.of(
                        UserRole.builder()
                                .role(UserRole.Role.USER)
                                .build()
                ))
                .userSocialList(List.of())
                .build();
        userRepository.save(user);
    }
}
