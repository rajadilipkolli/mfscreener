package com.learning.mfscreener.mapper;

import static com.learning.mfscreener.utils.AppConstants.FLEXIBLE_DATE_FORMATTER;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.repository.MFSchemeTypeRepository;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MfSchemeDtoToEntityMapperHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeDtoToEntityMapperHelper.class);

    // Define the regular expressions
    private static final Pattern TYPE_CATEGORY_SUBCATEGORY_PATTERN =
            Pattern.compile("^[^()]+\\([^()]*?\\s*-\\s*[^()]*\\)$");

    private final MFSchemeTypeRepository mfSchemeTypeRepository;

    public MfSchemeDtoToEntityMapperHelper(MFSchemeTypeRepository mfSchemeTypeRepository) {
        this.mfSchemeTypeRepository = mfSchemeTypeRepository;
    }

    @AfterMapping
    void updateMFScheme(MFSchemeDTO scheme, @MappingTarget MFSchemeEntity mfSchemeEntity) {
        MFSchemeNavEntity mfSchemenavEntity = new MFSchemeNavEntity();
        mfSchemenavEntity.setNav("N.A.".equals(scheme.nav()) ? 0F : Float.parseFloat(scheme.nav()));
        // Use the flexible formatter to parse the date
        LocalDate parsedDate = LocalDate.parse(scheme.date(), FLEXIBLE_DATE_FORMATTER);
        mfSchemenavEntity.setNavDate(parsedDate);
        mfSchemeEntity.addSchemeNav(mfSchemenavEntity);

        MFSchemeTypeEntity mfSchemeTypeEntity = null;
        String schemeType = scheme.schemeType();
        Matcher matcher = TYPE_CATEGORY_SUBCATEGORY_PATTERN.matcher(schemeType);
        if (matcher.find()) {
            String type = matcher.group(1).strip();
            String category = matcher.group(2).strip();
            String subCategory = matcher.group(3).strip();
            mfSchemeTypeEntity = findOrCreateMFSchemeTypeEntity(type, category, subCategory, mfSchemeTypeRepository);
        } else {
            if (!schemeType.contains("-")) {
                String type = schemeType.substring(0, schemeType.indexOf('('));
                String category = schemeType.substring(schemeType.indexOf('(') + 1, schemeType.length() - 1);
                mfSchemeTypeEntity = findOrCreateMFSchemeTypeEntity(type, category, null, mfSchemeTypeRepository);
            } else {
                LOGGER.error("Unable to parse schemeType :{}", schemeType);
            }
        }
        mfSchemeEntity.setMfSchemeTypeEntity(mfSchemeTypeEntity);
    }

    MFSchemeTypeEntity findOrCreateMFSchemeTypeEntity(
            String type, String category, @Nullable String subCategory, MFSchemeTypeRepository mfSchemeTypeRepository) {
        MFSchemeTypeEntity byTypeAndCategoryAndSubCategory =
                mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
        if (byTypeAndCategoryAndSubCategory == null) {
            MFSchemeTypeEntity mfSchemeType = new MFSchemeTypeEntity();
            mfSchemeType.setType(type);
            mfSchemeType.setCategory(category);
            mfSchemeType.setSubCategory(subCategory);
            byTypeAndCategoryAndSubCategory = mfSchemeTypeRepository.save(mfSchemeType);
        }
        return byTypeAndCategoryAndSubCategory;
    }
}
