package com.learning.mfscreener.service;

import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.repository.MFSchemeTypeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class MFSchemeTypeService {

    private final MFSchemeTypeRepository mfSchemeTypeRepository;

    public MFSchemeTypeService(MFSchemeTypeRepository mfSchemeTypeRepository) {
        this.mfSchemeTypeRepository = mfSchemeTypeRepository;
    }

    @Cacheable(cacheNames = "mfSchemeTypeEntity")
    public MFSchemeTypeEntity findByTypeAndCategoryAndSubCategory(String type, String category, String subCategory) {
        return mfSchemeTypeRepository
                .findByTypeAndCategoryAndSubCategory(type, category, subCategory)
                .orElseGet(() -> {
                    MFSchemeTypeEntity mfSchemeType = new MFSchemeTypeEntity();
                    mfSchemeType.setType(type);
                    mfSchemeType.setCategory(category);
                    mfSchemeType.setSubCategory(subCategory);
                    return mfSchemeTypeRepository.save(mfSchemeType);
                });
    }
}
