package com.example.shop.order.domain.repository;

import com.example.shop.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    Page<Order> findAll(Pageable pageable);

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    long count();
}
