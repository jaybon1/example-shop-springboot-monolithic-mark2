package com.example.shop.payment.infrastructure.persistence.repository;

import com.example.shop.order.infrastructure.persistence.entity.OrderEntity;
import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.infrastructure.persistence.entity.PaymentEntity;
import com.example.shop.payment.infrastructure.persistence.mapper.PaymentMapper;
import com.example.shop.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper paymentMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentEntity paymentEntity;
        if (payment.getId() != null) {
            paymentEntity = paymentJpaRepository.findById(payment.getId())
                    .orElseGet(() -> paymentMapper.toEntity(payment));
            paymentMapper.applyDomain(payment, paymentEntity);
        } else {
            paymentEntity = paymentMapper.toEntity(payment);
        }
        assignRelations(payment, paymentEntity);
        PaymentEntity savedPaymentEntity = paymentJpaRepository.save(paymentEntity);
        return paymentMapper.toDomain(savedPaymentEntity);
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public long count() {
        return paymentJpaRepository.count();
    }

    private void assignRelations(Payment payment, PaymentEntity paymentEntity) {
        if (payment.getOrderId() != null) {
            OrderEntity orderReference = entityManager.getReference(OrderEntity.class, payment.getOrderId());
            orderReference.assignPayment(paymentEntity);
        }
        if (payment.getUserId() != null) {
            UserEntity userReference = entityManager.getReference(UserEntity.class, payment.getUserId());
            paymentEntity.assignUser(userReference);
        }
    }
}
