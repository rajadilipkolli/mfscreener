package com.example.mfscreener.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mfscreener.entities.*;
import com.example.mfscreener.models.*;
import com.example.mfscreener.repository.CASDetailsEntityRepository;
import com.example.mfscreener.repository.InvestorInfoEntityRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NavServiceImplTest {

    @Mock
    private InvestorInfoEntityRepository investorInfoEntityRepository;

    @Mock
    private CASDetailsEntityRepository casDetailsEntityRepository;

    @InjectMocks
    private NavServiceImpl navService;

    @Test
    void findDeltaWithNoNewTransactions() {
        // given
        BDDMockito.given(casDetailsEntityRepository.findByInvestorInfoEntity_EmailAndInvestorInfoEntity_Name(
                        "junit@email.com", "name"))
                .willReturn(getInvestorInfoEntity());
        // when
        UserCASDetailsEntity userCASDetailsEntity = navService.findDelta("junit@email.com", "name", getCasDTO());
        // then
        assertThat(userCASDetailsEntity).isNotNull();
    }

    @Test
    void findDeltaWithNewTransactionAdded() {
        // given
        BDDMockito.given(casDetailsEntityRepository.findByInvestorInfoEntity_EmailAndInvestorInfoEntity_Name(
                        "junit@email.com", "name"))
                .willReturn(getInvestorInfoEntity());
        // when
        CasDTO casDto = getCasDTO();
        casDto.folios()
                .get(0)
                .schemes()
                .get(0)
                .transactions()
                .add(new UserTransactionDTO(
                        LocalDate.parse("2023-04-28"), null, 20.4d, 8.65d, 150.0d, 100d, "PURCHASE", null));
        UserCASDetailsEntity userCASDetailsEntity = navService.findDelta("junit@email.com", "name", casDto);
        // then
        assertThat(userCASDetailsEntity).isNotNull();
        assertThat(userCASDetailsEntity
                        .getFolioEntities()
                        .get(0)
                        .getSchemeEntities()
                        .get(0)
                        .getTransactionEntities()
                        .size())
                .isEqualTo(2);
    }

    private UserCASDetailsEntity getInvestorInfoEntity() {
        UserCASDetailsEntity userCasDetails = new UserCASDetailsEntity();
        userCasDetails.setId(1L);
        userCasDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        UserFolioDetailsEntity userFolioEntity = new UserFolioDetailsEntity();
        userFolioEntity.setId(1L);
        userFolioEntity.setPan("pan");
        userFolioEntity.setFolio("folio");
        UserSchemeDetailsEntity userSchemeEntity = new UserSchemeDetailsEntity();
        userSchemeEntity.setId(51L);
        userSchemeEntity.setAmfi(119544L);
        userSchemeEntity.setScheme(
                "Aditya Birla Sun Life Tax Relief'96 Fund- (ELSS U/S 80C of IT ACT) - Growth-Direct Plan");
        UserTransactionDetailsEntity transactionEntity = new UserTransactionDetailsEntity();
        transactionEntity.setId(51L);
        transactionEntity.setTransactionDate(LocalDate.of(2017, 9, 15));
        transactionEntity.setType("PURCHASE");
        transactionEntity.setUnits(16.595d);
        userSchemeEntity.getTransactionEntities().add(transactionEntity);
        userFolioEntity.getSchemeEntities().add(userSchemeEntity);
        userCasDetails.getFolioEntities().add(userFolioEntity);
        InvestorInfoEntity investorInfo = new InvestorInfoEntity();
        userCasDetails.setInvestorInfoEntity(investorInfo);
        investorInfo.setUserCasDetailsEntity(userCasDetails);
        return userCasDetails;
    }

    private CasDTO getCasDTO() {
        List<UserFolioDTO> foliosList = new ArrayList<>();
        List<UserSchemeDTO> schemasList = new ArrayList<>();
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2017-09-15"),
                "Purchase - Event Trigger",
                500.0d,
                16.595d,
                30.13d,
                16.569d,
                "PURCHASE",
                null);
        transactions.add(userTransactionDTO);
        UserSchemeDTO userSchemeDto1 = new UserSchemeDTO(
                "Aditya Birla Sun Life Tax Relief'96 Fund- (ELSS U/S 80C of IT ACT) - Growth-Direct Plan",
                "INF209K01UN8",
                119544L,
                "DIRECT",
                "B02GZ",
                "EQUITY",
                "CAMS",
                "0.0",
                "16.662",
                "16.662",
                null,
                transactions);
        schemasList.add(userSchemeDto1);
        UserFolioDTO userFolioDTO1 = new UserFolioDTO("folio", "amc", "pan", "kyc", "panKyc", schemasList);
        foliosList.add(userFolioDTO1);
        return new CasDTO(
                null, null, null, new InvestorInfoDTO("junit@email.com", "name", "9848022338", "address"), foliosList);
    }
}
