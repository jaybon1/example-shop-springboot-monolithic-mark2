package com.example.shop.common.infrastructure.config.schedule;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableAsync(mode = AdviceMode.ASPECTJ) // mode = AdviceMode.ASPECTJ 스캐줄끼리 시간 간섭 없이 각각 작동
public class ScheduleConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10); // 스레드 풀 크기 조정, 프로젝트가 가지고 있는 스캐줄 개수 이상으로 세팅
        return threadPoolTaskScheduler;
    }

}