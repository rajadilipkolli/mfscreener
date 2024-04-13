package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.repository.MFSchemeTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Loggable
public class MFSchemeTypeService {

    private final MFSchemeTypeRepository mfSchemeTypeRepository;

    public MFSchemeTypeService(MFSchemeTypeRepository mfSchemeTypeRepository) {
        this.mfSchemeTypeRepository = mfSchemeTypeRepository;
    }

    @Transactional
    public MFSchemeTypeEntity saveSchemeType(MFSchemeTypeEntity mfSchemeType) {
        return mfSchemeTypeRepository.save(mfSchemeType);
    }

    public MFSchemeTypeEntity findByTypeAndCategoryAndSubCategory(String type, String category, String subCategory) {
        return mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }
}
