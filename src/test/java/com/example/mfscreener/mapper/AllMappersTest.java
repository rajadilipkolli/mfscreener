/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mfscreener.adapter.ConversionServiceAdapter;
import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.entities.MFSchemeNav;
import com.example.mfscreener.models.MFSchemeDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = AllMappersTest.SpringTestConfig.class)
class AllMappersTest {

    @Configuration
    @ComponentScan(basePackageClasses = ConversionServiceAdapter.class)
    public static class SpringTestConfig {

        @Autowired private ConversionService conversionService;
    }

    @Autowired private ConversionServiceAdapter conversionServiceAdapter;

    @Test
    @Disabled
    public void testMfSchemeToMfSchemeDTOMapper() {
        MFScheme mfScheme = new MFScheme();

        MFSchemeDTO target = conversionServiceAdapter.mapMFSchemeToMFSchemeDTO(mfScheme);

        assertThat(target).isNull();
        mfScheme = new MFScheme();
        mfScheme.setSchemeId(1L);
        mfScheme.setSchemeName("JunitScheme");
        mfScheme.setPayOut("dividend");
        List<MFSchemeNav> mfSchemenavies = new ArrayList<>();
        MFSchemeNav mfSchemenav = new MFSchemeNav();
        mfSchemenav.setNav(22.45D);
        mfSchemenav.setNavDate(LocalDate.of(2022, 1, 1));
        mfSchemenavies.add(mfSchemenav);
        mfScheme.setMfSchemeNavies(mfSchemenavies);
        target = conversionServiceAdapter.mapMFSchemeToMFSchemeDTO(mfScheme);
        assertThat(target).isNotNull();
        assertThat(target.schemeCode()).isEqualTo("1");
        assertThat(target.nav()).isEqualTo("1980");
        assertThat(target.schemeName()).isEqualTo("JunitScheme");
        assertThat(target.date()).isEqualTo("1980");
        assertThat(target.payout()).isEqualTo("dividend");
    }
}
