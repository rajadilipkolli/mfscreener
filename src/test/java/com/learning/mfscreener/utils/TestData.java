package com.learning.mfscreener.utils;

import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.InvestorInfoDTO;
import com.learning.mfscreener.models.portfolio.StatementPeriodDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestData {

    public CasDTO getCasDTO() {
        List<UserFolioDTO> foliosList = new ArrayList<>();
        List<UserSchemeDTO> schemasList = new ArrayList<>();
        UserSchemeDTO userSchemeDTO = getUserSchemeDTO();
        schemasList.add(userSchemeDTO);
        UserFolioDTO userFolioDTO = new UserFolioDTO(
                "101998485", "Aditya Birla Sun Life Mutual Fund", "ABCDE1234F", "OK", "OK", schemasList);
        foliosList.add(userFolioDTO);
        return new CasDTO(
                new StatementPeriodDTO("01-Jan-1990", "20-Jun-2023"),
                "CAMS",
                "DETAILED",
                new InvestorInfoDTO("junit@email.com", "Junit", "9848022338", "address"),
                foliosList);
    }

    private static UserSchemeDTO getUserSchemeDTO() {
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
        return new UserSchemeDTO(
                "Aditya Birla Sun Life Tax Relief'96 Fund- (ELSS U/S 80C of IT ACT) - Growth-Direct Plan - ISIN: INF209K01UN8",
                "INF209K01UN8",
                null,
                "DIRECT",
                "B02GZ",
                "EQUITY",
                "CAMS",
                "0.0",
                "16.662",
                "16.662",
                null,
                transactions);
    }
}
