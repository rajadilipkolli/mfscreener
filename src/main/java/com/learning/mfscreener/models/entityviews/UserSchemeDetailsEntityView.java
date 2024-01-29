package com.learning.mfscreener.models.entityviews;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EntityView for {@link com.learning.mfscreener.entities.UserSchemeDetailsEntity}
 */
@EntityView(UserSchemeDetailsEntity.class)
public interface UserSchemeDetailsEntityView {
    String getCreatedBy();

    LocalDateTime getCreatedDate();

    String getLastModifiedBy();

    LocalDateTime getLastModifiedDate();

    @IdMapping
    Long getId();

    String getScheme();

    String getIsin();

    String getAdvisor();

    String getRtaCode();

    String getRta();

    String getType();

    Long getAmfi();

    String getMyopen();

    String getClose();

    String getCloseCalculated();

    List<UserTransactionDetailsEntityView> getTransactionEntities();
}
