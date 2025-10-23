package com.example.shop.common.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@MappedSuperclass
@FilterDef(
        name = "notDeleted",
        defaultCondition = "deleted_at IS NULL",
        autoEnabled = true,
        applyToLoadByKey = true
)
// 서비스에서 비활성화하려면 em.unwrap(Session.class).disableFilter("notDeleted");
@Filter(name = "notDeleted")
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    public void markDeleted(Instant deletedAt, UUID userId) {
        this.deletedAt = deletedAt;
        this.deletedBy = userId.toString();
    }

    @PrePersist
    protected void onPrePersist() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.createdBy == null) {
            this.createdBy = "system";
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.updatedBy == null) {
            this.updatedBy = this.createdBy;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedAt = Instant.now();
        if (this.updatedBy == null) {
            this.updatedBy = this.createdBy;
        }
    }

}
