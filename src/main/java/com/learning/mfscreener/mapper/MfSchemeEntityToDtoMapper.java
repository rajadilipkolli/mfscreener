/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.mapper;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.models.MFSchemeDTO;
import java.time.LocalDate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

@Mapper(config = MapperSpringConfig.class)
public interface MfSchemeEntityToDtoMapper extends Converter<MFSchemeEntity, MFSchemeDTO> {

    @Mapping(target = "date", ignore = true)
    @Mapping(target = "nav", ignore = true)
    @Mapping(target = "schemeCode", source = "schemeId")
    @Mapping(target = "payout", source = "payOut")
    @Mapping(target = "amc", source = "fundHouse")
    @Mapping(target = "schemeType", ignore = true)
    @Override
    MFSchemeDTO convert(MFSchemeEntity mfSchemeEntity);

    @AfterMapping
    default MFSchemeDTO updateMFScheme(MFSchemeEntity mfSchemeEntity, @MappingTarget MFSchemeDTO mfSchemeDTO) {
        String date = null;
        String nav = null;
        if (!mfSchemeEntity.getMfSchemeNavEntities().isEmpty()) {
            LocalDate localDate = mfSchemeEntity.getMfSchemeNavEntities().getFirst().getNavDate();
            nav = String.valueOf(mfSchemeEntity.getMfSchemeNavEntities().getFirst().getNav());
            if (null != localDate) {
                date = localDate.toString();
            }
        }
        MFSchemeTypeEntity mfSchemeTypeEntity = mfSchemeEntity.getMfSchemeTypeEntity();
        String subCategory = mfSchemeTypeEntity.getSubCategory();
        String category = mfSchemeTypeEntity.getCategory();
        String categoryAndSubCategory;
        if (StringUtils.hasText(subCategory)) {
            categoryAndSubCategory = category + " - " + subCategory;
        } else {
            categoryAndSubCategory = category;
        }
        return mfSchemeDTO.withNavAndDateAndSchemeType(
                mfSchemeTypeEntity.getType() + "(" + categoryAndSubCategory + ")", nav, date);
    }
}
