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

    public CasDTO getCasDTO(boolean addFolio, boolean addScheme) {
        List<UserFolioDTO> foliosList = new ArrayList<>();
        List<UserSchemeDTO> schemasList = new ArrayList<>();
        schemasList.add(getIciciSchemeDTO());
        if (addScheme) {
            schemasList.add(getIciciTechnologyScheme());
        }
        UserFolioDTO userFolioDTO = new UserFolioDTO(
                "15936342 / 43", "ICICI Prudential Mutual Fund", "ABCDE1234F", "OK", "OK", schemasList);
        foliosList.add(userFolioDTO);
        if (addFolio) {
            userFolioDTO = new UserFolioDTO(
                    "91095687154 / 0", "AXIS Mutual Fund", "ABCDE1234F", "OK", "OK", List.of(axisSchemeDTO()));
            foliosList.add(userFolioDTO);
        }
        return new CasDTO(
                new StatementPeriodDTO("01-Jan-1990", "20-Jun-2023"),
                "CAMS",
                "DETAILED",
                new InvestorInfoDTO("junit@email.com", "Junit", "9848022338", "address"),
                foliosList);
    }

    private static UserSchemeDTO getIciciTechnologyScheme() {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2021-03-31"),
                "SIP Purchase - INA100009859",
                100.0d,
                0.859d,
                116.4d,
                0.859d,
                "PURCHASE_SIP",
                null);
        transactions.add(userTransactionDTO);
        return new UserSchemeDTO(
                "ICICI Prudential Technology Fund - Direct Plan - Growth (Non-Demat) - ISIN: INF109K01Z48",
                "INF109K01Z48",
                120594L,
                "INA200005166",
                "P8019",
                "EQUITY",
                "CAMS",
                "0.0",
                "86.696",
                "86.696",
                null,
                transactions);
    }

    private static UserSchemeDTO axisSchemeDTO() {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2017-09-20"), "Purchase", 1000.0d, 23.711d, 42.1747d, 23.711d, "PURCHASE", null);
        transactions.add(userTransactionDTO);
        return new UserSchemeDTO(
                "Axis ELSS Tax Saver Fund - Direct Growth - ISIN: INF846K01EW2",
                "INF846K01EW2",
                120503L,
                "INA000006651",
                "128TSDGG",
                "EQUITY",
                "KFINTECH",
                "0.0",
                "206.719",
                "206.719",
                null,
                transactions);
    }

    private static UserSchemeDTO getIciciSchemeDTO() {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2021-01-14"),
                "SIP Purchase-BSE - - INA200005166",
                499.98d,
                15.965d,
                31.3182d,
                15.965d,
                "PURCHASE_SIP",
                null);
        transactions.add(userTransactionDTO);
        return new UserSchemeDTO(
                "ICICI Prudential Nifty Next 50 Index Fund - Direct Plan - Growth (Non-Demat) - ISIN: INF109K01Y80",
                "INF109K01Y80",
                null,
                "INA200005166",
                "P8107",
                "EQUITY",
                "CAMS",
                "0.0",
                "3801.107",
                "3801.107",
                null,
                transactions);
    }
}
