package com.example.mfscreener.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<T> {

  @CreatedBy protected T createdBy;

  @CreatedDate protected LocalDateTime createdDate;

  @LastModifiedBy protected T lastModifiedBy;

  @LastModifiedDate protected LocalDateTime lastModifiedDate;
}
