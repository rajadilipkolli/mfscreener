package com.learning.mfscreener.models.entityviews;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * EntityView for {@link com.learning.mfscreener.entities.UserTransactionDetailsEntity}
 */
@EntityView(UserTransactionDetailsEntity.class)
public interface UserTransactionDetailsEntityView {
    String getCreatedBy();

    LocalDateTime getCreatedDate();

    String getLastModifiedBy();

    LocalDateTime getLastModifiedDate();

    @IdMapping
    Long getId();

    LocalDate getTransactionDate();

    String getDescription();

    Double getAmount();

    Double getUnits();

    Double getNav();

    Double getBalance();

    String getType();

    String getDividendRate();
}
