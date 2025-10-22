package com.example.shopmark2.product.infrastructure.persistence.repository;

import com.example.shopmark2.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findByName(String name);

    Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
