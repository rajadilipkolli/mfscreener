/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mfscreener.adapter.ConversionServiceAdapter;
import com.example.mfscreener.common.AbstractIntegrationTest;
import com.example.mfscreener.entities.*;
import com.example.mfscreener.models.*;
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

    @Test
    void testMapCasDTOToUserCASDetailsEntity() {
        UserCASDetailsEntity target =
                this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(null);
        assertThat(target).isNull();

        List<UserFolioDTO> userFolioDTOS = new ArrayList<>();
        List<UserSchemeDTO> userSchemeDTOS = new ArrayList<>();
        ValuationDTO valuationDTO = null;
        List<UserTransactionDTO> userTransactionDTOS = new ArrayList<>();
        UserTransactionDTO transactionDTO =
                new UserTransactionDTO(
                        "2022-01-31", "", "499.95", "50", "23.45", "100.45", "SIP", null);
        userTransactionDTOS.add(transactionDTO);
        UserSchemeDTO userSchemeDTO =
                new UserSchemeDTO(
                        "scheme",
                        "isin",
                        12053L,
                        "advisor",
                        "rtaCode",
                        "type",
                        "rta",
                        null,
                        null,
                        null,
                        valuationDTO,
                        userTransactionDTOS);
        userSchemeDTOS.add(userSchemeDTO);
        UserFolioDTO userFolioDTO =
                new UserFolioDTO("123456", "SBI", "ABCDE1234F", "OK", "OK", userSchemeDTOS);
        userFolioDTOS.add(userFolioDTO);
        StatementPeriodDTO statementPeriod = new StatementPeriodDTO("2020-02-02", "2022-12-31");
        InvestorInfoDTO investorInfo =
                new InvestorInfoDTO("junit@email.com", "junit", "9848022338", "JunitAddress");
        CasDTO casDTO =
                new CasDTO(
                        statementPeriod,
                        FileTypeEnum.CAMS.toString(),
                        CasTypeEnum.DETAILED.toString(),
                        investorInfo,
                        userFolioDTOS);
        target = this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
        assertThat(target).isNotNull();
        assertThat(target.getCasTypeEnum()).isEqualTo(CasTypeEnum.DETAILED);
        assertThat(target.getFileTypeEnum()).isEqualTo(FileTypeEnum.CAMS);
        assertThat(target.getId()).isNull();
        assertThat(target.getCreatedBy()).isNull();
        assertThat(target.getCreatedDate()).isNull();
        assertThat(target.getLastModifiedBy()).isNull();
        assertThat(target.getLastModifiedDate()).isNull();

        InvestorInfoEntity investorInfoEntity = target.getInvestorInfoEntity();
        assertThat(investorInfoEntity).isNotNull();
        assertThat(investorInfoEntity.getId()).isNull();
        assertThat(investorInfoEntity.getCreatedBy()).isNull();
        assertThat(investorInfoEntity.getCreatedDate()).isNull();
        assertThat(investorInfoEntity.getLastModifiedBy()).isNull();
        assertThat(investorInfoEntity.getLastModifiedDate()).isNull();
        assertThat(investorInfoEntity.getEmail()).isEqualTo("junit@email.com");
        assertThat(investorInfoEntity.getName()).isEqualTo("junit");
        assertThat(investorInfoEntity.getMobile()).isEqualTo("9848022338");
        assertThat(investorInfoEntity.getAddress()).isEqualTo("JunitAddress");

        List<UserFolioDetailsEntity> folioEntities = target.getFolioEntities();
        assertThat(folioEntities).isNotEmpty().hasSize(1);
        UserFolioDetailsEntity userFolioDetailsEntity = folioEntities.get(0);
        assertThat(userFolioDetailsEntity).isNotNull();
    }
}
