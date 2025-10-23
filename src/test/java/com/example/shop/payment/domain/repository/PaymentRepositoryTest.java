package com.example.shop.payment.domain.repository;

import com.example.shop.common.infrastructure.config.jpa.JpaAuditConfig;
import com.example.shop.common.infrastructure.config.jpa.QuerydslConfig;
import com.example.shop.common.infrastructure.config.jpa.audit.CustomAuditAware;
import com.example.shop.order.infrastructure.persistence.entity.OrderEntity;
import com.example.shop.order.infrastructure.persistence.entity.OrderItemEntity;
import com.example.shop.payment.domain.model.Payment;
import com.example.shop.user.infrastructure.persistence.entity.UserEntity;
import com.example.shop.user.infrastructure.persistence.entity.UserRoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditConfig.class, CustomAuditAware.class, QuerydslConfig.class,
        com.example.shop.payment.infrastructure.persistence.repository.PaymentRepositoryImpl.class,
        com.example.shop.payment.infrastructure.persistence.mapper.PaymentMapper.class})
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("결제를 저장하고 다시 조회할 수 있다")
    void saveAndFindById() {
        PersistContext context = createOrderAndUser();
        Payment payment = Payment.builder()
                .orderId(context.orderEntity.getId())
                .userId(context.userEntity.getId())
                .status(Payment.Status.COMPLETED)
                .method(Payment.Method.CARD)
                .amount(1000L)
                .transactionKey("tx-12345")
                .build();

        Payment saved = paymentRepository.save(payment);
        Payment found = paymentRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAmount()).isEqualTo(payment.getAmount());
        assertThat(found.getOrderId()).isEqualTo(payment.getOrderId());
        assertThat(found.getUserId()).isEqualTo(payment.getUserId());
    }

    private PersistContext createOrderAndUser() {
        UserEntity userEntity = UserEntity.builder()
                .username("payment-user")
                .password("payment-pass")
                .nickname("pay")
                .email("pay@example.com")
                .build();
        userEntity.add(UserRoleEntity.builder().role(UserRoleEntity.Role.USER).build());
        testEntityManager.persistAndFlush(userEntity);

        OrderEntity orderEntity = OrderEntity.builder()
                .user(userEntity)
                .status(OrderEntity.Status.CREATED)
                .totalAmount(1000L)
                .build();
        orderEntity.addOrderItem(OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("product")
                .unitPrice(1000L)
                .quantity(1L)
                .lineTotal(1000L)
                .build());
        testEntityManager.persistAndFlush(orderEntity);

        return new PersistContext(userEntity, orderEntity);
    }

    private record PersistContext(UserEntity userEntity, OrderEntity orderEntity) {
    }
}
