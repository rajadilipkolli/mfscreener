/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.mapper;

import com.example.mfscreener.entities.MFSchemeEntity;
import com.example.mfscreener.entities.MFSchemeNavEntity;
import com.example.mfscreener.models.MFSchemeDTO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface MfSchemeDtoToEntityMapper extends Converter<MFSchemeDTO, MFSchemeEntity> {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Mapping(target = "mfSchemeTypeEntity", ignore = true)
    @Mapping(target = "mfSchemeNavEntities", ignore = true)
    @Mapping(target = "schemeNameAlias", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "fundHouse", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "payOut", source = "payout")
    @Mapping(target = "schemeId", source = "schemeCode")
    @Override
    MFSchemeEntity convert(MFSchemeDTO scheme);

    @AfterMapping
    default void updateMFScheme(MFSchemeDTO scheme, @MappingTarget MFSchemeEntity mfSchemeEntity) {
        MFSchemeNavEntity mfSchemenavEntity = new MFSchemeNavEntity();
        mfSchemenavEntity.setNav(
                "N.A.".equals(scheme.nav()) ? 0D : Double.parseDouble(scheme.nav()));
        mfSchemenavEntity.setNavDate(LocalDate.parse(scheme.date(), DATE_FORMATTER));
        mfSchemeEntity.addSchemeNav(mfSchemenavEntity);
    }
}
