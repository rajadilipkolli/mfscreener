/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.mapper;

import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.models.NAVDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface NavDataToMFSchemeNavMapper extends Converter<NAVDataDTO, MFSchemeNavEntity> {

    @Mapping(target = "mfSchemeEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "navDate", source = "date")
    @Mapping(source = "schemeId", target = "mfSchemeEntity.schemeId")
    @Override
    MFSchemeNavEntity convert(NAVDataDTO navDataDTO);
}
