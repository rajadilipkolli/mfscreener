/*
 * Copyright 2014 - 2024 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.learning.mfscreener.config.db;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.learning.mfscreener.models.entityviews.InvestorInfoEntityView;
import com.learning.mfscreener.models.entityviews.UserCASDetailsEntityView;
import com.learning.mfscreener.models.entityviews.UserFolioDetailsEntityView;
import com.learning.mfscreener.models.entityviews.UserSchemeDetailsEntityView;
import com.learning.mfscreener.models.entityviews.UserTransactionDetailsEntityView;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration(proxyBeanMethods = false)
public class BlazePersistenceConfiguration {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Lazy(false)
    CriteriaBuilderFactory createCriteriaBuilderFactory() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        return config.createCriteriaBuilderFactory(entityManagerFactory);
    }

    @Bean
    EntityViewConfiguration createEntityViewConfiguration() {
        EntityViewConfiguration config = EntityViews.createDefaultConfiguration();
        config.addEntityView(InvestorInfoEntityView.class);
        config.addEntityView(UserCASDetailsEntityView.class);
        config.addEntityView(UserFolioDetailsEntityView.class);
        config.addEntityView(UserSchemeDetailsEntityView.class);
        config.addEntityView(UserTransactionDetailsEntityView.class);
        return config;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Lazy(false)
    EntityViewManager createEntityViewManager(
            CriteriaBuilderFactory cbf, EntityViewConfiguration entityViewConfiguration) {
        return entityViewConfiguration.createEntityViewManager(cbf);
    }
}
