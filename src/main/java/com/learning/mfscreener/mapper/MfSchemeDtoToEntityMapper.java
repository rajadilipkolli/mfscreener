/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.mapper;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.repository.MFSchemeTypeRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapper(config = MapperSpringConfig.class)
public abstract class MfSchemeDtoToEntityMapper {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    // Define the regular expressions
    private static final Pattern TYPE_CATEGORY_SUBCATEGORY_PATTERN =
            Pattern.compile("^(.*?)\\((.*?)\\s*-\\s*(.*?)\\)$");

    @Mapping(target = "mfSchemeTypeEntity", ignore = true)
    @Mapping(target = "mfSchemeNavEntities", ignore = true)
    @Mapping(target = "schemeNameAlias", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "fundHouse", source = "amc")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "payOut", source = "payout")
    @Mapping(target = "schemeId", source = "schemeCode")
    @Mapping(target = "version", ignore = true)
    public abstract MFSchemeEntity mapMFSchemeDTOToMFSchemeEntity(
            MFSchemeDTO scheme, @Context MFSchemeTypeRepository mfSchemeTypeRepository);

    @AfterMapping
    void updateMFScheme(
            MFSchemeDTO scheme,
            @MappingTarget MFSchemeEntity mfSchemeEntity,
            @Context MFSchemeTypeRepository mfSchemeTypeRepository) {
        MFSchemeNavEntity mfSchemenavEntity = new MFSchemeNavEntity();
        mfSchemenavEntity.setNav("N.A.".equals(scheme.nav()) ? 0F : Float.parseFloat(scheme.nav()));
        mfSchemenavEntity.setNavDate(LocalDate.parse(scheme.date(), AppConstants.FORMATTER_DD_MMM_YYYY));
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
                log.error("Unable to parse schemeType :{}", schemeType);
            }
        }
        mfSchemeEntity.setMfSchemeTypeEntity(mfSchemeTypeEntity);
    }

    MFSchemeTypeEntity findOrCreateMFSchemeTypeEntity(
            String type, String category, String subCategory, MFSchemeTypeRepository mfSchemeTypeRepository) {
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
