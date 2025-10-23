package com.example.shop.payment.infrastructure.persistence.entity;

import com.example.shop.global.infrastructure.persistence.entity.BaseEntity;
import com.example.shop.order.infrastructure.persistence.entity.OrderEntity;
import com.example.shop.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table(name = "PAYMENT")
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.COMPLETED;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private Method method;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "transaction_key")
    private String transactionKey;

    public void markCompleted() {
        this.status = Status.COMPLETED;
    }

    public void markCancelled() {
        this.status = Status.CANCELLED;
    }

    public void assignOrder(OrderEntity orderEntity) {
        this.order = orderEntity;
    }

    public void assignUser(UserEntity userEntity) {
        this.user = userEntity;
    }

    public void updateDetails(Method method, Long amount, String transactionKey) {
        if (method != null) {
            this.method = method;
        }
        if (amount != null) {
            this.amount = amount;
        }
        if (transactionKey != null) {
            this.transactionKey = transactionKey;
        }
    }

    public enum Status {
        COMPLETED,
        CANCELLED
    }

    public enum Method {
        CARD,
        BANK_TRANSFER,
        MOBILE,
        POINT
    }
}
