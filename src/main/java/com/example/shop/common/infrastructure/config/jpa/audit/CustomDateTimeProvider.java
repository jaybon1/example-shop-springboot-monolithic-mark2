package com.example.shop.common.infrastructure.config.jpa.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomDateTimeProvider implements DateTimeProvider {

    private final Clock clock = Clock.systemUTC();

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(Instant.now(clock));
    }

}
