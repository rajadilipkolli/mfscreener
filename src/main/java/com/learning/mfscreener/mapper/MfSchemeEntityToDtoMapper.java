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
        if (!mfSchemeEntity.getMfSchemeNavEntities().isEmpty()) {
            Float navDouble = mfSchemeEntity.getMfSchemeNavEntities().get(0).getNav();
            LocalDate localDate = mfSchemeEntity.getMfSchemeNavEntities().get(0).getNavDate();
            String nav = String.valueOf(navDouble);
            String date = null;
            if (null != localDate) {
                date = localDate.toString();
            }
            return mfSchemeDTO.withNavAndDate(nav, date);
        }
        MFSchemeTypeEntity mfSchemeTypeEntity = mfSchemeEntity.getMfSchemeTypeEntity();
        return mfSchemeDTO.withSchemeType(mfSchemeTypeEntity.getType() + "(" + mfSchemeTypeEntity.getCategory() + ")");
    }
}
