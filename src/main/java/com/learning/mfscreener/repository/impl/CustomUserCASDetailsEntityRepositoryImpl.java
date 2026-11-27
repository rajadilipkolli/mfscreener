package com.learning.mfscreener.repository.impl;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.models.entityviews.UserCASDetailsEntityView;
import com.learning.mfscreener.repository.CustomUserCASDetailsEntityRepository;
import jakarta.persistence.EntityManager;

public class CustomUserCASDetailsEntityRepositoryImpl implements CustomUserCASDetailsEntityRepository {

    private final EntityManager entityManager;

    private final CriteriaBuilderFactory criteriaBuilderFactory;

    private final EntityViewManager entityViewManager;

    public CustomUserCASDetailsEntityRepositoryImpl(
            EntityManager entityManager,
            CriteriaBuilderFactory criteriaBuilderFactory,
            EntityViewManager entityViewManager) {
        this.entityManager = entityManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
        this.entityViewManager = entityViewManager;
    }

    @Override
    public UserCASDetailsEntityView findByInvestorEmailAndName(String email, String name) {
        return entityViewManager
                .applySetting(
                        EntityViewSetting.create(UserCASDetailsEntityView.class),
                        criteriaBuilderFactory.create(entityManager, UserCASDetailsEntity.class))
                .where("investorInfoEntity.email")
                .eq(email)
                .where("investorInfoEntity.name")
                .eq(name) // Adding condition for the name
                .getSingleResult();
    }
}
