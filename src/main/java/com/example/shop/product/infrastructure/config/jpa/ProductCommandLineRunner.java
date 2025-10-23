package com.example.shop.product.infrastructure.config.jpa;

import com.example.shop.product.domain.model.Product;
import com.example.shop.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class ProductCommandLineRunner implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            saveProductBy("안성탕면 5개 번들", 3000L, 125L);
            saveProductBy("LA갈비 1kg", 25000L, 95L);
            saveProductBy("동원참치 1캔", 2100L, 500L);
        }
    }

    private void saveProductBy(String name, Long price, Long stock) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .build();
        productRepository.save(product);
    }

}
