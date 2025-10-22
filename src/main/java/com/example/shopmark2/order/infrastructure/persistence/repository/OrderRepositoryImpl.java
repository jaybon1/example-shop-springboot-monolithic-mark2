package com.example.shopmark2.order.infrastructure.persistence.repository;

import com.example.shopmark2.order.domain.model.Order;
import com.example.shopmark2.order.domain.repository.OrderRepository;
import com.example.shopmark2.order.infrastructure.persistence.entity.OrderEntity;
import com.example.shopmark2.order.infrastructure.persistence.mapper.OrderMapper;
import com.example.shopmark2.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity orderEntity;
        if (order.getId() != null) {
            orderEntity = orderJpaRepository.findById(order.getId())
                    .orElseGet(() -> orderMapper.toEntity(order));
            orderMapper.applyDomain(order, orderEntity);
        } else {
            orderEntity = orderMapper.toEntity(order);
        }
        assignUser(order, orderEntity);
        OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);
        return orderMapper.toDomain(savedOrderEntity);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId)
                .map(orderMapper::toDomain);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderJpaRepository.findAll(pageable)
                .map(orderMapper::toDomain);
    }

    @Override
    public Page<Order> findByUserId(UUID userId, Pageable pageable) {
        return orderJpaRepository.findByUser_Id(userId, pageable)
                .map(orderMapper::toDomain);
    }

    @Override
    public long count() {
        return orderJpaRepository.count();
    }

    private void assignUser(Order order, OrderEntity orderEntity) {
        UUID userId = order.getUserId();
        if (userId == null) {
            return;
        }
        UserEntity userReference = entityManager.getReference(UserEntity.class, userId);
        orderEntity.assignUser(userReference);
    }
}
