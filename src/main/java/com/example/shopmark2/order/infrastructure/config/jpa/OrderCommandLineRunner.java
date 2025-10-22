package com.example.shopmark2.order.infrastructure.config.jpa;

import com.example.shopmark2.order.domain.model.Order;
import com.example.shopmark2.order.domain.model.OrderItem;
import com.example.shopmark2.order.domain.repository.OrderRepository;
import com.example.shopmark2.product.domain.model.Product;
import com.example.shopmark2.product.domain.repository.ProductRepository;
import com.example.shopmark2.user.domain.model.User;
import com.example.shopmark2.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class OrderCommandLineRunner implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return;
        }

        List<Product> productList = productRepository.findAll(Pageable.unpaged()).getContent();
        if (productList.isEmpty()) {
            return;
        }

        List<User> userList = userRepository.searchUsers(null, null, null, Pageable.unpaged()).getContent();
        if (userList.isEmpty()) {
            return;
        }

        User firstUser = userList.get(0);
        Product firstProduct = productList.get(0);
        savePaidOrder(firstUser, firstProduct, 2L);

        Product lastProduct = productList.get(productList.size() - 1);
        User secondUser = userList.size() > 1 ? userList.get(1) : firstUser;
        savePaidOrder(secondUser, lastProduct, 1L);
    }

    private void savePaidOrder(User user, Product product, long quantity) {
        Order order = Order.builder()
                .userId(user.getId())
                .status(Order.Status.CREATED)
                .orderItemList(List.of())
                .totalAmount(0L)
                .build();

        long unitPrice = Optional.ofNullable(product.getPrice()).orElse(0L);
        long lineTotal = unitPrice * quantity;

        OrderItem orderItem = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();

        order = order.addOrderItem(orderItem).updateTotalAmount(lineTotal);
        orderRepository.save(order);
    }
}
