package com.example.shop.order.infrastructure.persistence.entity;

import com.example.shop.global.infrastructure.persistence.entity.BaseEntity;
import com.example.shop.payment.infrastructure.persistence.entity.PaymentEntity;
import com.example.shop.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`ORDER`")
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Builder.Default
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItemList = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private PaymentEntity payment;

    public void addOrderItem(OrderItemEntity orderItemEntity) {
        orderItemEntity.setOrder(this);
        this.orderItemList.add(orderItemEntity);
    }

    public void updateTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void assignPayment(PaymentEntity paymentEntity) {
        if (paymentEntity != null) {
            paymentEntity.assignOrder(this);
        }
        this.payment = paymentEntity;
    }

    public void assignUser(UserEntity userEntity) {
        this.user = userEntity;
    }

    public void updateStatus(Status status) {
        if (status != null) {
            this.status = status;
        }
    }

    public enum Status {
        CREATED,
        PAID,
        CANCELLED
    }
}
