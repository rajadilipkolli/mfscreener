package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MFSchemeTypeRepository extends JpaRepository<MFSchemeTypeEntity, Integer> {

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "mfSchemeTypeEntity", unless = "#result == null")
    MFSchemeTypeEntity findByTypeAndCategoryAndSubCategory(String type, String category, String subCategory);
}
