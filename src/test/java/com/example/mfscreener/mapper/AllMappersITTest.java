/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mfscreener.adapter.ConversionServiceAdapter;
import com.example.mfscreener.common.AbstractIntegrationTest;
import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.entities.MFSchemeNav;
import com.example.mfscreener.models.MFSchemeDTO;
import com.example.mfscreener.models.NAVData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AllMappersITTest extends AbstractIntegrationTest {

    @Autowired private ConversionServiceAdapter conversionServiceAdapter;

    @Test
    void testMfSchemeToDTOMapper() {

        MFSchemeDTO target = conversionServiceAdapter.mapMFSchemeToMFSchemeDTO(null);
        assertThat(target).isNull();

        MFScheme mfScheme = new MFScheme();
        target = conversionServiceAdapter.mapMFSchemeToMFSchemeDTO(mfScheme);
        assertThat(target).isNotNull();
        assertThat(target.schemeCode()).isNull();
        assertThat(target.nav()).isNull();
        assertThat(target.schemeName()).isNull();
        assertThat(target.date()).isNull();
        assertThat(target.payout()).isNull();

        mfScheme.setSchemeId(1L);
        mfScheme.setSchemeName("JunitScheme");
        mfScheme.setPayOut("dividend");
        target = conversionServiceAdapter.mapMFSchemeToMFSchemeDTO(mfScheme);
        assertThat(target).isNotNull();
        assertThat(target.schemeCode()).isEqualTo("1");
        assertThat(target.nav()).isNull();
        assertThat(target.schemeName()).isEqualTo("JunitScheme");
        assertThat(target.date()).isNull();
        assertThat(target.payout()).isEqualTo("dividend");

        List<MFSchemeNav> mfSchemeNavs = new ArrayList<>();
        MFSchemeNav mfSchemenav = new MFSchemeNav();
        mfSchemenav.setNav(22.45D);
        mfSchemenav.setNavDate(LocalDate.of(2022, 1, 1));
        mfSchemeNavs.add(mfSchemenav);
        mfScheme.setMfSchemeNavies(mfSchemeNavs);
        target = conversionServiceAdapter.mapMFSchemeToMFSchemeDTO(mfScheme);
        assertThat(target).isNotNull();
        assertThat(target.schemeCode()).isEqualTo("1");
        assertThat(target.nav()).isEqualTo("22.45");
        assertThat(target.schemeName()).isEqualTo("JunitScheme");
        assertThat(target.date()).isEqualTo("2022-01-01");
        assertThat(target.payout()).isEqualTo("dividend");
    }

    @Test
    void testMfSchemeDtoToEntityMapper() {
        MFScheme target = conversionServiceAdapter.mapMFSchemeDTOToMFScheme(null);
        assertThat(target).isNull();

        MFSchemeDTO mfScheme =
                new MFSchemeDTO("1", "dividend", "JunitSchemeName", "22.34", "23-Nov-2022");
        target = conversionServiceAdapter.mapMFSchemeDTOToMFScheme(mfScheme);
        assertThat(target).isNotNull();
        assertThat(target.getSchemeId()).isEqualTo(1);
        assertThat(target.getSchemeName()).isEqualTo("JunitSchemeName");
        assertThat(target.getPayOut()).isEqualTo("dividend");
        assertThat(target.getFundHouse()).isNull();
        assertThat(target.getMfSchemeType()).isNull();
        assertThat(target.getSchemeNameAlias()).isNull();
        assertThat(target.getMfSchemeNavies()).isNotEmpty().hasSize(1);
        assertThat(target.getMfSchemeNavies().get(0).getNav()).isEqualTo(22.34);
        assertThat(target.getMfSchemeNavies().get(0).getNavDate())
                .isEqualTo(LocalDate.of(2022, 11, 23));

        mfScheme = new MFSchemeDTO("1", "dividend", "JunitSchemeName", "N.A.", "23-Nov-2022");
        target = conversionServiceAdapter.mapMFSchemeDTOToMFScheme(mfScheme);
        assertThat(target).isNotNull();
        assertThat(target.getSchemeId()).isEqualTo(1);
        assertThat(target.getSchemeName()).isEqualTo("JunitSchemeName");
        assertThat(target.getPayOut()).isEqualTo("dividend");
        assertThat(target.getFundHouse()).isNull();
        assertThat(target.getMfSchemeType()).isNull();
        assertThat(target.getSchemeNameAlias()).isNull();
        assertThat(target.getMfSchemeNavies()).isNotEmpty().hasSize(1);
        assertThat(target.getMfSchemeNavies().get(0).getNav()).isEqualTo(0);
        assertThat(target.getMfSchemeNavies().get(0).getNavDate())
                .isEqualTo(LocalDate.of(2022, 11, 23));
    }

    @Test
    void testMapNAVDataToMFSchemeNav() {
        MFSchemeNav target = this.conversionServiceAdapter.mapNAVDataToMFSchemeNav(null);
        assertThat(target).isNull();

        NAVData navData = new NAVData("01-01-2022", "20.45");
        target = this.conversionServiceAdapter.mapNAVDataToMFSchemeNav(navData);
        assertThat(target).isNotNull();
        assertThat(target.getNavDate()).isEqualTo(LocalDate.of(2022, 1, 1));
        assertThat(target.getNav()).isEqualTo(20.45);
        assertThat(target.getMfScheme()).isNull();
        assertThat(target.getId()).isNull();
        assertThat(target.getCreatedBy()).isNull();
        assertThat(target.getCreatedDate()).isNull();
        assertThat(target.getLastModifiedBy()).isNull();
        assertThat(target.getLastModifiedDate()).isNull();
    }
}
