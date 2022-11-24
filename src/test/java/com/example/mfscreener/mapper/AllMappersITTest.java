/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mfscreener.adapter.ConversionServiceAdapter;
import com.example.mfscreener.common.AbstractIntegrationTest;
import com.example.mfscreener.entities.MFSchemeEntity;
import com.example.mfscreener.entities.MFSchemeNavEntity;
import com.example.mfscreener.models.MFSchemeDTO;
import com.example.mfscreener.models.NAVDataDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AllMappersITTest extends AbstractIntegrationTest {

    @Autowired private ConversionServiceAdapter conversionServiceAdapter;

    @Test
    void testMfSchemeToDTOMapper() {

        MFSchemeDTO target = conversionServiceAdapter.mapMFSchemeEntityToMFSchemeDTO(null);
        assertThat(target).isNull();

        MFSchemeEntity mfSchemeEntity = new MFSchemeEntity();
        target = conversionServiceAdapter.mapMFSchemeEntityToMFSchemeDTO(mfSchemeEntity);
        assertThat(target).isNotNull();
        assertThat(target.schemeCode()).isNull();
        assertThat(target.nav()).isNull();
        assertThat(target.schemeName()).isNull();
        assertThat(target.date()).isNull();
        assertThat(target.payout()).isNull();

        mfSchemeEntity.setSchemeId(1L);
        mfSchemeEntity.setSchemeName("JunitScheme");
        mfSchemeEntity.setPayOut("dividend");
        target = conversionServiceAdapter.mapMFSchemeEntityToMFSchemeDTO(mfSchemeEntity);
        assertThat(target).isNotNull();
        assertThat(target.schemeCode()).isEqualTo("1");
        assertThat(target.nav()).isNull();
        assertThat(target.schemeName()).isEqualTo("JunitScheme");
        assertThat(target.date()).isNull();
        assertThat(target.payout()).isEqualTo("dividend");

        List<MFSchemeNavEntity> mfSchemeNavEntities = new ArrayList<>();
        MFSchemeNavEntity mfSchemenavEntity = new MFSchemeNavEntity();
        mfSchemenavEntity.setNav(22.45D);
        mfSchemenavEntity.setNavDate(LocalDate.of(2022, 1, 1));
        mfSchemeNavEntities.add(mfSchemenavEntity);
        mfSchemeEntity.setMfSchemeNavEntities(mfSchemeNavEntities);
        target = conversionServiceAdapter.mapMFSchemeEntityToMFSchemeDTO(mfSchemeEntity);
        assertThat(target).isNotNull();
        assertThat(target.schemeCode()).isEqualTo("1");
        assertThat(target.nav()).isEqualTo("22.45");
        assertThat(target.schemeName()).isEqualTo("JunitScheme");
        assertThat(target.date()).isEqualTo("2022-01-01");
        assertThat(target.payout()).isEqualTo("dividend");
    }

    @Test
    void testMfSchemeDtoToEntityMapper() {
        MFSchemeEntity target = conversionServiceAdapter.mapMFSchemeDTOToMFSchemeEntity(null);
        assertThat(target).isNull();

        MFSchemeDTO mfScheme =
                new MFSchemeDTO("1", "dividend", "JunitSchemeName", "22.34", "23-Nov-2022");
        target = conversionServiceAdapter.mapMFSchemeDTOToMFSchemeEntity(mfScheme);
        assertThat(target).isNotNull();
        assertThat(target.getSchemeId()).isEqualTo(1);
        assertThat(target.getSchemeName()).isEqualTo("JunitSchemeName");
        assertThat(target.getPayOut()).isEqualTo("dividend");
        assertThat(target.getFundHouse()).isNull();
        assertThat(target.getMfSchemeTypeEntity()).isNull();
        assertThat(target.getSchemeNameAlias()).isNull();
        assertThat(target.getMfSchemeNavEntities()).isNotEmpty().hasSize(1);
        assertThat(target.getMfSchemeNavEntities().get(0).getNav()).isEqualTo(22.34);
        assertThat(target.getMfSchemeNavEntities().get(0).getNavDate())
                .isEqualTo(LocalDate.of(2022, 11, 23));

        mfScheme = new MFSchemeDTO("1", "dividend", "JunitSchemeName", "N.A.", "23-Nov-2022");
        target = conversionServiceAdapter.mapMFSchemeDTOToMFSchemeEntity(mfScheme);
        assertThat(target).isNotNull();
        assertThat(target.getSchemeId()).isEqualTo(1);
        assertThat(target.getSchemeName()).isEqualTo("JunitSchemeName");
        assertThat(target.getPayOut()).isEqualTo("dividend");
        assertThat(target.getFundHouse()).isNull();
        assertThat(target.getMfSchemeTypeEntity()).isNull();
        assertThat(target.getSchemeNameAlias()).isNull();
        assertThat(target.getMfSchemeNavEntities()).isNotEmpty().hasSize(1);
        assertThat(target.getMfSchemeNavEntities().get(0).getNav()).isEqualTo(0);
        assertThat(target.getMfSchemeNavEntities().get(0).getNavDate())
                .isEqualTo(LocalDate.of(2022, 11, 23));
    }

    @Test
    void testMapNAVDataToMFSchemeNav() {
        MFSchemeNavEntity target =
                this.conversionServiceAdapter.mapNAVDataDTOToMFSchemeNavEntity(null);
        assertThat(target).isNull();

        NAVDataDTO navDataDTO = new NAVDataDTO("01-01-2022", "20.45");
        target = this.conversionServiceAdapter.mapNAVDataDTOToMFSchemeNavEntity(navDataDTO);
        assertThat(target).isNotNull();
        assertThat(target.getNavDate()).isEqualTo(LocalDate.of(2022, 1, 1));
        assertThat(target.getNav()).isEqualTo(20.45);
        assertThat(target.getMfSchemeEntity()).isNull();
        assertThat(target.getId()).isNull();
        assertThat(target.getCreatedBy()).isNull();
        assertThat(target.getCreatedDate()).isNull();
        assertThat(target.getLastModifiedBy()).isNull();
        assertThat(target.getLastModifiedDate()).isNull();
    }
}
