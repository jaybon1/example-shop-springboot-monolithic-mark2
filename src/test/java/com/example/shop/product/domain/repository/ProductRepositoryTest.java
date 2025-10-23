package com.example.shop.product.domain.repository;

import com.example.shop.common.infrastructure.config.jpa.JpaAuditConfig;
import com.example.shop.common.infrastructure.config.jpa.QuerydslConfig;
import com.example.shop.common.infrastructure.config.jpa.audit.CustomAuditAware;
import com.example.shop.product.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditConfig.class, CustomAuditAware.class, QuerydslConfig.class,
        com.example.shop.product.infrastructure.persistence.repository.ProductRepositoryImpl.class,
        com.example.shop.product.infrastructure.persistence.mapper.ProductMapper.class})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품명을 통해 상품을 조회할 수 있다")
    void findByNameReturnsProduct() {
        Product saved = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(1000L)
                .stock(5L)
                .build());

        Optional<Product> optional = productRepository.findByName("테스트상품");

        assertThat(optional).isPresent();
        assertThat(optional.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("미존재 상품명 조회는 빈 Optional을 반환한다")
    void findByNameReturnsEmptyWhenMissing() {
        Optional<Product> optional = productRepository.findByName("없는상품");

        assertThat(optional).isEmpty();
    }

    @Test
    @DisplayName("상품명 부분 검색은 페이지로 결과를 반환한다")
    void findByNameContainingIgnoreCaseReturnsPage() {
        productRepository.save(Product.builder()
                .name("테스트상품A")
                .price(1000L)
                .stock(5L)
                .build());
        productRepository.save(Product.builder()
                .name("기타상품")
                .price(2000L)
                .stock(3L)
                .build());

        Page<Product> page = productRepository.findByNameContainingIgnoreCase("상품", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
