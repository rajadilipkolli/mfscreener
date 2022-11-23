/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.models.MFSchemeDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface MfSchemeToMfSchemeDTOMapper extends Converter<MFScheme, MFSchemeDTO> {

    @Mapping(target = "date", ignore = true)
    @Mapping(target = "nav", ignore = true)
    @Mapping(target = "schemeCode", source = "schemeId")
    @Mapping(target = "payout", source = "payOut")
    @Override
    MFSchemeDTO convert(MFScheme mfScheme);

    @AfterMapping
    default MFSchemeDTO updateMFScheme(MFScheme mfScheme, @MappingTarget MFSchemeDTO mfSchemeDTO) {
        if (!mfScheme.getMfSchemeNavies().isEmpty()) {
            var navDouble = mfScheme.getMfSchemeNavies().get(0).getNav();
            var localDate = mfScheme.getMfSchemeNavies().get(0).getNavDate();
            String nav = null;
            if (null != navDouble) {
                nav = String.valueOf(navDouble);
            }
            String date = null;
            if (null != localDate) {
                date = localDate.toString();
            }
            return mfSchemeDTO.withNavAndDate(nav, date);
        }
        return mfSchemeDTO;
    }
}
