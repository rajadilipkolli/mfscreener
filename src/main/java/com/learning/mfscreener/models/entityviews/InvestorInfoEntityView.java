package com.learning.mfscreener.models.entityviews;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.learning.mfscreener.entities.InvestorInfoEntity;
import java.time.LocalDateTime;

/**
 * EntityView for {@link com.learning.mfscreener.entities.InvestorInfoEntity}
 */
@EntityView(InvestorInfoEntity.class)
public interface InvestorInfoEntityView {
    String getCreatedBy();

    LocalDateTime getCreatedDate();

    String getLastModifiedBy();

    LocalDateTime getLastModifiedDate();

    @IdMapping
    Long getId();

    String getEmail();

    String getName();

    String getMobile();

    String getAddress();
}
