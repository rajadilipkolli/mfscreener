/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.entities;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity<T> {

    @CreatedBy
    protected T createdBy;

    @CreatedDate
    protected LocalDateTime createdDate;

    @LastModifiedBy
    protected T lastModifiedBy;

    @LastModifiedDate
    protected LocalDateTime lastModifiedDate;

    public T getCreatedBy() {
        return createdBy;
    }

    public AuditableEntity<T> setCreatedBy(T createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public AuditableEntity<T> setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public T getLastModifiedBy() {
        return lastModifiedBy;
    }

    public AuditableEntity<T> setLastModifiedBy(T lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public AuditableEntity<T> setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }
}
