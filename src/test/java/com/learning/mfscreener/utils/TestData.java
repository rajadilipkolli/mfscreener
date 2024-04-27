package com.learning.mfscreener.utils;

import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.InvestorInfoDTO;
import com.learning.mfscreener.models.portfolio.StatementPeriodDTO;
import com.learning.mfscreener.models.portfolio.TransactionType;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.portfolio.ValuationDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestData {

    public static CasDTO getCasDTO(boolean addFolio, boolean addScheme, boolean addTransaction) {
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
                    "91095687154 / 0", "AXIS Mutual Fund", "", "OK", "NOT OK", List.of(axisSchemeDTO(addTransaction)));
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
                TransactionType.PURCHASE_SIP,
                null);
        UserTransactionDTO userTransactionDTO1 = new UserTransactionDTO(
                LocalDate.parse("2021-05-24"),
                "SIP Purchase - INA100009859",
                100.0d,
                0.823d,
                121.58d,
                0.823d,
                TransactionType.PURCHASE_SIP,
                null);
        UserTransactionDTO buyTransaction = new UserTransactionDTO(
                LocalDate.parse("2022-09-08"),
                "Purchase-BSE - - INA200005166",
                999.95d,
                6.954d,
                143.79d,
                58.584d,
                TransactionType.PURCHASE,
                null);
        UserTransactionDTO buyTransactionTax = new UserTransactionDTO(
                LocalDate.parse("2022-09-08"),
                "*** Stamp Duty ***",
                0.05d,
                null,
                null,
                null,
                TransactionType.STAMP_DUTY_TAX,
                null);
        UserTransactionDTO sellTransaction = new UserTransactionDTO(
                LocalDate.parse("2022-09-08"),
                "*Redemption - ELECTRONIC PAYMENT-BSE - - N256222117703332 ,\t\tess STT l",
                -1000.0,
                -6.955,
                143.79,
                51.629,
                TransactionType.REDEMPTION,
                null);
        UserTransactionDTO sellTransactionTax = new UserTransactionDTO(
                LocalDate.parse("2022-09-08"),
                "*** SSTT Paid ***",
                0.01,
                null,
                null,
                null,
                TransactionType.STT_TAX,
                null);
        transactions.add(userTransactionDTO);
        transactions.add(userTransactionDTO1);
        transactions.add(buyTransaction);
        transactions.add(buyTransactionTax);
        transactions.add(sellTransaction);
        transactions.add(sellTransactionTax);
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
                new ValuationDTO("2024-04-12", 190.11, 0),
                transactions);
    }

    private static UserSchemeDTO axisSchemeDTO(boolean addTransaction) {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2017-09-20"),
                "Purchase",
                1000.0d,
                23.711d,
                42.1747d,
                23.711d,
                TransactionType.PURCHASE,
                null);
        transactions.add(userTransactionDTO);
        if (addTransaction) {
            transactions.add(new UserTransactionDTO(
                    LocalDate.parse("2017-09-22"),
                    "Purchase",
                    500.0d,
                    12.031d,
                    41.5579d,
                    35.742d,
                    TransactionType.PURCHASE,
                    null));
        }
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
                new ValuationDTO("2024-04-12", 97.352, 0),
                transactions);
    }

    private static UserSchemeDTO getIciciSchemeDTO() {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2021-07-19"),
                "Switch In - From Liquid Fund - DP Growth - INA000006651",
                24383.78d,
                153.371d,
                158.9851d,
                153.371d,
                TransactionType.SWITCH_IN,
                null);
        UserTransactionDTO taxTransaction = new UserTransactionDTO(
                LocalDate.parse("2021-07-19"),
                "*** Stamp Duty ***",
                1.22d,
                null,
                null,
                null,
                TransactionType.STAMP_DUTY_TAX,
                null);
        UserTransactionDTO sellUserTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2022-08-01"),
                "*Switch Out - To Nifty 50 Index Fund-DP Growth-BSE - , less STT",
                -5000.0d,
                -28.261d,
                176.9251d,
                125.110d,
                TransactionType.SWITCH_OUT,
                null);
        UserTransactionDTO sellTaxTransaction = new UserTransactionDTO(
                LocalDate.parse("2022-08-01"),
                "*** STT Paid ***",
                0.05d,
                null,
                null,
                null,
                TransactionType.STT_TAX,
                null);
        UserTransactionDTO userTransactionDTO1 = new UserTransactionDTO(
                LocalDate.parse("2021-01-14"),
                "SIP Purchase-BSE - - INA200005166",
                499.98d,
                15.965d,
                31.3182d,
                15.965d,
                TransactionType.PURCHASE_SIP,
                null);
        transactions.add(userTransactionDTO);
        transactions.add(taxTransaction);
        transactions.add(sellUserTransactionDTO);
        transactions.add(sellTaxTransaction);
        transactions.add(userTransactionDTO1);
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
                new ValuationDTO("2024-04-12", 58.1998, 0),
                transactions);
    }

    public static CasDTO getCasDTO() {
        CasDTO casDTO = getCasDTO(true, true, true);
        casDTO.folios()
                .add(new UserFolioDTO(
                        "17755325221 / 0",
                        "Canara Robeco Mutual Fund",
                        "ABCDE1234F",
                        "OK",
                        "OK",
                        List.of(getCanaraScheme())));
        casDTO.folios().get(0).schemes().add(getICICIUSBlueChipScheme());
        casDTO.folios()
                .get(0)
                .schemes()
                .get(0)
                .transactions()
                .add(new UserTransactionDTO(
                        LocalDate.parse("2021-02-04"),
                        "Switch In - From Multicap Fund - DP - Growth-BSE - - INA200005166",
                        100d,
                        3.221d,
                        31.0496d,
                        19.186d,
                        TransactionType.SWITCH_IN,
                        null));
        return casDTO;
    }

    private static UserSchemeDTO getICICIUSBlueChipScheme() {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2020-06-22"),
                "SIP Purchase Appln : 72611 - INA100006898",
                100d,
                2.796d,
                35.76d,
                2.796d,
                TransactionType.PURCHASE_SIP,
                null);
        transactions.add(userTransactionDTO);
        return new UserSchemeDTO(
                "ICICI Prudential US Bluechip Equity Fund - Direct Plan - Growth (Non-Demat) - ISIN: INF109K01Z71",
                "INF109K01Z71",
                120186L,
                "INA200005166",
                "P8133",
                "EQUITY",
                "CAMS",
                "0.0",
                "109.583",
                "109.583",
                new ValuationDTO("2024-04-12", 63.04, 0),
                transactions);
    }

    private static UserSchemeDTO getCanaraScheme() {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2023-06-15"),
                "Systematic Investment (1)",
                2999.85d,
                10.359d,
                289.6d,
                10.359d,
                TransactionType.PURCHASE_SIP,
                null);
        transactions.add(userTransactionDTO);
        return new UserSchemeDTO(
                "Canara Robeco Equity Hybrid Fund - Direct Growth - ISIN: INF760K01EZ8",
                "INF760K01EZ8",
                118272L,
                "INA200005166",
                "101GBDGG",
                "EQUITY",
                "KFINTECH",
                "0.0",
                "192.868",
                "192.868",
                new ValuationDTO("2024-04-12", 351.49, 0),
                transactions);
    }
}
