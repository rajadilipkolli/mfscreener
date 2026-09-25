package com.learning.mfscreener.models.entityviews;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EntityView for {@link com.learning.mfscreener.entities.UserFolioDetailsEntity}
 */
@EntityView(UserFolioDetailsEntity.class)
public interface UserFolioDetailsEntityView {
    String getCreatedBy();

    LocalDateTime getCreatedDate();

    String getLastModifiedBy();

    LocalDateTime getLastModifiedDate();

    @IdMapping
    Long getId();

    String getFolio();

    String getAmc();

    String getPan();

    String getKyc();

    @Mapping(fetch = FetchStrategy.MULTISET)
    List<UserSchemeDetailsEntityView> getSchemeEntities();
}
