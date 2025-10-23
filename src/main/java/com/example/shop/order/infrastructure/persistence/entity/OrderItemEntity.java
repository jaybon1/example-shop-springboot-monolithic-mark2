package com.example.shop.order.infrastructure.persistence.entity;

import com.example.shop.common.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table(name = "ORDER_ITEM")
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class OrderItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private OrderEntity order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "line_total", nullable = false)
    private Long lineTotal;

    public void update(UUID productId, String productName, Long unitPrice, Long quantity, Long lineTotal) {
        if (productId != null) {
            this.productId = productId;
        }
        if (productName != null) {
            this.productName = productName;
        }
        if (unitPrice != null) {
            this.unitPrice = unitPrice;
        }
        if (quantity != null) {
            this.quantity = quantity;
        }
        if (lineTotal != null) {
            this.lineTotal = lineTotal;
        }
    }

    public void detachOrder() {
        this.order = null;
    }
}
