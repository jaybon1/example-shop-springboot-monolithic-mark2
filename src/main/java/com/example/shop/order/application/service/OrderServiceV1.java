package com.example.shop.order.application.service;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.Order.Status;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.order.presentation.advice.OrderError;
import com.example.shop.order.presentation.advice.OrderException;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.product.domain.model.Product;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceV1 {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public ResGetOrdersDtoV1 getOrders(UUID authUserId, List<String> authUserRoleList, Pageable pageable) {
        Page<Order> orderPage;
        if (isAdminOrManager(authUserRoleList)) {
            orderPage = orderRepository.findAll(pageable);
        } else {
            orderPage = orderRepository.findByUserId(authUserId, pageable);
        }
        return ResGetOrdersDtoV1.builder()
                .orderPage(new ResGetOrdersDtoV1.OrderPageDto(orderPage))
                .build();
    }

    public ResGetOrderDtoV1 getOrder(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
        Order order = getOrderForUser(orderId, authUserId, authUserRoleList);
        return ResGetOrderDtoV1.of(order);
    }

    @Transactional
    public ResPostOrdersDtoV1 postOrders(UUID authUserId, ReqPostOrdersDtoV1 reqDto) {

        List<ReqPostOrdersDtoV1.OrderDto.OrderItemDto> reqOrderItemList = reqDto.getOrder().getOrderItemList();

        Set<UUID> productIdSet = reqOrderItemList.stream()
                .map(ReqPostOrdersDtoV1.OrderDto.OrderItemDto::getProductId)
                .collect(Collectors.toSet());

        Map<UUID, Product> productMap = productRepository.findAllById(productIdSet)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (productMap.size() != productIdSet.size()) {
            throw new OrderException(OrderError.ORDER_PRODUCT_NOT_FOUND);
        }

        User orderUser = userRepository.findDefaultById(authUserId);

        Order order = Order.builder()
                .userId(orderUser.getId())
                .status(Status.CREATED)
                .orderItemList(List.of())
                .totalAmount(0L)
                .build();

        long totalAmount = 0L;
        for (ReqPostOrdersDtoV1.OrderDto.OrderItemDto reqOrderItem : reqOrderItemList) {
            UUID productId = reqOrderItem.getProductId();
            Long quantityValue = reqOrderItem.getQuantity();

            Product product = productMap.get(productId);

            long quantity = quantityValue;
            long currentStock = Optional.ofNullable(product.getStock()).orElse(0L);
            long updatedStock = currentStock - quantity;
            if (updatedStock < 0) {
                throw new OrderException(OrderError.ORDER_PRODUCT_OUT_OF_STOCK);
            }

            Product savedProduct = productRepository.save(product.update(null, null, updatedStock));
            productMap.put(productId, savedProduct);

            long lineTotal = safeMultiply(Optional.ofNullable(product.getPrice()).orElse(0L), quantity);
            totalAmount = safeAdd(totalAmount, lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(quantityValue)
                    .lineTotal(lineTotal)
                    .build();
            order = order.addOrderItem(orderItem);
        }

        order = order.updateTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return ResPostOrdersDtoV1.of(savedOrder, null);
    }

    @Transactional
    public void postOrderCancel(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
        Order order = getOrderForUser(orderId, authUserId, authUserRoleList);

        if (Status.CANCELLED.equals(order.getStatus())) {
            throw new OrderException(OrderError.ORDER_ALREADY_CANCELLED);
        }

        Payment payment = order.getPayment();
        if (Status.PAID.equals(order.getStatus())) {
            if (payment == null) {
                throw new PaymentException(PaymentError.PAYMENT_NOT_FOUND);
            }
            payment = handlePaymentCancellation(payment, authUserId, authUserRoleList);
        } else if (payment != null) {
            payment = handlePaymentCancellation(payment, authUserId, authUserRoleList);
        }

        restoreProductStock(order);
        Order cancelledOrder = order.markCancelled();
        if (payment != null) {
            Payment savedPayment = paymentRepository.save(payment);
            cancelledOrder = cancelledOrder.assignPayment(savedPayment);
        }
        orderRepository.save(cancelledOrder);
    }

    private Payment handlePaymentCancellation(Payment payment, UUID authUserId, List<String> authUserRoleList) {
        if (Payment.Status.CANCELLED.equals(payment.getStatus())) {
            throw new PaymentException(PaymentError.PAYMENT_ALREADY_CANCELLED);
        }

        if (!payment.isOwnedBy(authUserId) && !isAdminOrManager(authUserRoleList)) {
            throw new PaymentException(PaymentError.PAYMENT_FORBIDDEN);
        }

        return payment.markCancelled();
    }

    private void restoreProductStock(Order order) {
        List<OrderItem> orderItemList = order.getOrderItemList();
        if (orderItemList.isEmpty()) {
            return;
        }

        Set<UUID> productIds = orderItemList.stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toSet());

        Map<UUID, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (OrderItem orderItem : orderItemList) {
            Product product = productMap.get(orderItem.getProductId());
            if (product == null) {
                throw new OrderException(OrderError.ORDER_PRODUCT_NOT_FOUND);
            }

            long restoredStock = safeAdd(Optional.ofNullable(product.getStock()).orElse(0L), orderItem.getQuantity());
            Product savedProduct = productRepository.save(product.update(null, null, restoredStock));
            productMap.put(savedProduct.getId(), savedProduct);
        }
    }

    private Order getOrderForUser(UUID orderId, UUID authUserId, List<String> authUserRoleList) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderError.ORDER_NOT_FOUND));

        if (isAdminOrManager(authUserRoleList)) {
            return order;
        }

        if (authUserId != null && order.isOwnedBy(authUserId)) {
            return order;
        }

        throw new OrderException(OrderError.ORDER_FORBIDDEN);
    }

    private boolean isAdminOrManager(List<String> authUserRoleList) {
        if (authUserRoleList == null) {
            return false;
        }
        return authUserRoleList.contains(UserRole.Role.ADMIN.toString())
                || authUserRoleList.contains(UserRole.Role.MANAGER.toString());
    }

    private long safeMultiply(Long left, long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException e) {
            throw new OrderException(OrderError.ORDER_AMOUNT_OVERFLOW);
        }
    }

    private long safeAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException e) {
            throw new OrderException(OrderError.ORDER_AMOUNT_OVERFLOW);
        }
    }

}
