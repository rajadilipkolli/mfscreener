package com.learning.mfscreener.models.entityviews;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.learning.mfscreener.entities.CasTypeEnum;
import com.learning.mfscreener.entities.FileTypeEnum;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EntityView for {@link com.learning.mfscreener.entities.UserCASDetailsEntity}
 */
@EntityView(UserCASDetailsEntity.class)
public interface UserCASDetailsEntityView {
    String getCreatedBy();

    LocalDateTime getCreatedDate();

    String getLastModifiedBy();

    LocalDateTime getLastModifiedDate();

    @IdMapping
    Long getId();

    CasTypeEnum getCasTypeEnum();

    FileTypeEnum getFileTypeEnum();

    InvestorInfoEntityView getInvestorInfoEntity();

    @Mapping(fetch = FetchStrategy.MULTISET)
    List<UserFolioDetailsEntityView> getFolioEntities();
}
