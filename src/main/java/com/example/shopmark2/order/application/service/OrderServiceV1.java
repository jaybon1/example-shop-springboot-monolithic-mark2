package com.example.shopmark2.order.application.service;

import com.example.shopmark2.order.domain.model.Order;
import com.example.shopmark2.order.domain.model.Order.Status;
import com.example.shopmark2.order.domain.model.OrderItem;
import com.example.shopmark2.order.domain.repository.OrderRepository;
import com.example.shopmark2.order.presentation.advice.OrderError;
import com.example.shopmark2.order.presentation.advice.OrderException;
import com.example.shopmark2.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shopmark2.order.presentation.dto.response.ResGetOrdersDtoV1;
import com.example.shopmark2.order.presentation.dto.response.ResGetOrdersWithIdDtoV1;
import com.example.shopmark2.order.presentation.dto.response.ResPostOrdersDtoV1;
import com.example.shopmark2.payment.domain.model.Payment;
import com.example.shopmark2.payment.domain.repository.PaymentRepository;
import com.example.shopmark2.payment.presentation.advice.PaymentError;
import com.example.shopmark2.payment.presentation.advice.PaymentException;
import com.example.shopmark2.product.domain.model.Product;
import com.example.shopmark2.product.domain.repository.ProductRepository;
import com.example.shopmark2.user.domain.model.User;
import com.example.shopmark2.user.domain.model.UserRole;
import com.example.shopmark2.user.domain.repository.UserRepository;
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

    public ResGetOrdersWithIdDtoV1 getOrdersWithId(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
        Order order = getOrderForUser(orderId, authUserId, authUserRoleList);
        return ResGetOrdersWithIdDtoV1.of(order);
    }

    @Transactional
    public ResPostOrdersDtoV1 postOrders(UUID authUserId, ReqPostOrdersDtoV1 reqDto) {
        if (reqDto == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }

        ReqPostOrdersDtoV1.OrderDto reqOrder = reqDto.getOrder();
        if (reqOrder == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }

        List<ReqPostOrdersDtoV1.OrderDto.OrderItemDto> reqOrderItemList = reqOrder.getOrderItemList();
        if (reqOrderItemList == null || reqOrderItemList.isEmpty()) {
            throw new OrderException(OrderError.ORDER_ITEMS_EMPTY);
        }

        Set<UUID> productIdSet = reqOrderItemList.stream()
                .map(ReqPostOrdersDtoV1.OrderDto.OrderItemDto::getProductId)
                .collect(Collectors.toSet());

        if (productIdSet.contains(null)) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }

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

            if (quantityValue == null || quantityValue <= 0) {
                throw new OrderException(OrderError.ORDER_INVALID_QUANTITY);
            }

            Product product = productMap.get(productId);
            if (product == null) {
                throw new OrderException(OrderError.ORDER_PRODUCT_NOT_FOUND);
            }

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
    public void cancelOrdersWithId(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
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
