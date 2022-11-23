/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.models.MFSchemeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface MfSchemeToMfSchemeDTO extends Converter<MFScheme, MFSchemeDTO> {

    @Mapping(target = "schemeCode", source = "schemeName")
    @Mapping(target = "payout", source = "payOut")
    @Mapping(
            target = "nav",
            expression = "java(String.valueOf(mfScheme.getMfSchemeNavies().get(0).getNav()))")
    @Mapping(
            target = "date",
            expression = "java(String.valueOf(mfScheme.getMfSchemeNavies().get(0).getNavDate()))")
    @Override
    MFSchemeDTO convert(MFScheme mfScheme);
}
