/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.mapper;

import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.entities.MFSchemeNav;
import com.example.mfscreener.models.MFSchemeDTO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface NavServiceMapper extends Converter<MFSchemeDTO, MFScheme> {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Mapping(target = "schemeNameAlias", ignore = true)
    @Mapping(target = "payOut", ignore = true)
    @Mapping(target = "schemeId", source = "schemeCode")
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "fundHouse", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Override
    MFScheme convert(MFSchemeDTO scheme);

    @AfterMapping
    default void updateMFScheme(MFSchemeDTO scheme, @MappingTarget MFScheme mfScheme) {
        MFSchemeNav mfSchemenav = new MFSchemeNav();
        mfSchemenav.setNav("N.A.".equals(scheme.nav()) ? 0D : Double.parseDouble(scheme.nav()));
        mfSchemenav.setNavDate(LocalDate.parse(scheme.date(), DATE_FORMATTER));
        mfScheme.addSchemeNav(mfSchemenav);
    }
}
