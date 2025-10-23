package com.example.shop.order.domain.repository;

import com.example.shop.global.infrastructure.config.jpa.JpaAuditConfig;
import com.example.shop.global.infrastructure.config.jpa.QuerydslConfig;
import com.example.shop.global.infrastructure.config.jpa.audit.CustomAuditAware;
import com.example.shop.order.domain.model.Order;
import com.example.shop.order.infrastructure.persistence.entity.OrderEntity;
import com.example.shop.order.infrastructure.persistence.entity.OrderItemEntity;
import com.example.shop.user.infrastructure.persistence.entity.UserEntity;
import com.example.shop.user.infrastructure.persistence.entity.UserRoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditConfig.class, CustomAuditAware.class, QuerydslConfig.class,
        com.example.shop.order.infrastructure.persistence.repository.OrderRepositoryImpl.class,
        com.example.shop.order.infrastructure.persistence.mapper.OrderMapper.class,
        com.example.shop.payment.infrastructure.persistence.mapper.PaymentMapper.class})
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("사용자별 주문 목록을 조회할 수 있다")
    void findByUserIdReturnsPage() {
        UserEntity userEntity = createUser("order-user");
        testEntityManager.persistAndFlush(userEntity);

        OrderEntity orderEntity = OrderEntity.builder()
                .user(userEntity)
                .status(OrderEntity.Status.CREATED)
                .totalAmount(1000L)
                .build();
        orderEntity.addOrderItem(OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("sample")
                .unitPrice(1000L)
                .quantity(1L)
                .lineTotal(1000L)
                .build());
        testEntityManager.persist(orderEntity);
        testEntityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = orderRepository.findByUserId(userEntity.getId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getUserId()).isEqualTo(userEntity.getId());
    }

    private UserEntity createUser(String username) {
        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password("password123")
                .nickname("nick-" + username)
                .email(username + "@example.com")
                .build();
        userEntity.add(UserRoleEntity.builder()
                .role(UserRoleEntity.Role.USER)
                .build());
        return userEntity;
    }
}
