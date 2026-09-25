/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.mapper;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.models.MFSchemeDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = MapperSpringConfig.class,
        uses = MfSchemeDtoToEntityMapperHelper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MfSchemeDtoToEntityMapper {

    @Mapping(target = "version", ignore = true)
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
    MFSchemeEntity mapMFSchemeDTOToMFSchemeEntity(MFSchemeDTO scheme);
}
