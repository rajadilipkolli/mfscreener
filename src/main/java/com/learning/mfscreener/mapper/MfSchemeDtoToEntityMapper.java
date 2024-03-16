/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.mapper;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.service.MFSchemeTypeService;
import com.learning.mfscreener.utils.AppConstants;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Mapper(config = MapperSpringConfig.class)
public abstract class MfSchemeDtoToEntityMapper {
    // Define the regular expressions
    private static final Pattern TYPE_CATEGORY_SUBCATEGORY_PATTERN =
            Pattern.compile("^(.*?)\\((.*?)\\s*-\\s*(.*?)\\)$");

    @Autowired
    private MFSchemeTypeService mfSchemeTypeService;

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
    public abstract MFSchemeEntity mapMFSchemeDTOToMFSchemeEntity(MFSchemeDTO scheme);

    @AfterMapping
    void updateMFScheme(MFSchemeDTO scheme, @MappingTarget MFSchemeEntity mfSchemeEntity) {
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
            // Check if "-" is present
            String subCategory;
            if (matcher.group(3) != null) {
                subCategory = matcher.group(3).strip();
            } else {
                subCategory = null;
            }
            mfSchemeTypeEntity = mfSchemeTypeService.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
        } else {
            if (!schemeType.contains("-")) {
                String type = schemeType.substring(0, schemeType.indexOf('('));
                String category = schemeType.substring(schemeType.indexOf('(') + 1, schemeType.length() - 1);
                mfSchemeTypeEntity = mfSchemeTypeService.findByTypeAndCategoryAndSubCategory(type, category, null);
            } else {
                log.error("Unable to parse schemeType :{}", schemeType);
            }
        }
        mfSchemeEntity.setMfSchemeTypeEntity(mfSchemeTypeEntity);
    }
}
