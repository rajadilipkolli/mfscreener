package com.learning.mfscreener.utils;

import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.InvestorInfoDTO;
import com.learning.mfscreener.models.portfolio.StatementPeriodDTO;
import com.learning.mfscreener.models.portfolio.TransactionType;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.portfolio.ValuationDTO;
import java.math.BigDecimal;
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
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(0.859),
                BigDecimal.valueOf(116.4),
                BigDecimal.valueOf(0.859),
                TransactionType.PURCHASE_SIP,
                null);
        UserTransactionDTO userTransactionDTO1 = new UserTransactionDTO(
                LocalDate.parse("2021-05-24"),
                "SIP Purchase - INA100009859",
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(0.823),
                BigDecimal.valueOf(121.58),
                BigDecimal.valueOf(0.823),
                TransactionType.PURCHASE_SIP,
                null);
        UserTransactionDTO buyTransaction = new UserTransactionDTO(
                LocalDate.parse("2022-09-08"),
                "Purchase-BSE - - INA200005166",
                BigDecimal.valueOf(999.95),
                BigDecimal.valueOf(6.954),
                BigDecimal.valueOf(143.79),
                BigDecimal.valueOf(58.584),
                TransactionType.PURCHASE,
                null);
        UserTransactionDTO buyTransactionTax = new UserTransactionDTO(
                LocalDate.parse("2022-09-08"),
                "*** Stamp Duty ***",
                BigDecimal.valueOf(0.05),
                null,
                null,
                null,
                TransactionType.STAMP_DUTY_TAX,
                null);
        UserTransactionDTO sellTransaction = new UserTransactionDTO(
                LocalDate.parse("2022-09-08"),
                "*Redemption - ELECTRONIC PAYMENT-BSE - - N256222117703332 ,\t\tess STT l",
                BigDecimal.valueOf(-1000.0),
                BigDecimal.valueOf(-6.955),
                BigDecimal.valueOf(143.79),
                BigDecimal.valueOf(51.629),
                TransactionType.REDEMPTION,
                null);
        UserTransactionDTO sellTransactionTax = new UserTransactionDTO(
                LocalDate.parse("2022-09-08"),
                "*** SSTT Paid ***",
                BigDecimal.valueOf(0.01),
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
                BigDecimal.valueOf(1000.0),
                BigDecimal.valueOf(23.711),
                BigDecimal.valueOf(42.1747),
                BigDecimal.valueOf(23.711),
                TransactionType.PURCHASE,
                null);
        transactions.add(userTransactionDTO);
        if (addTransaction) {
            transactions.add(new UserTransactionDTO(
                    LocalDate.parse("2017-09-22"),
                    "Purchase",
                    BigDecimal.valueOf(500.0),
                    BigDecimal.valueOf(12.031),
                    BigDecimal.valueOf(41.5579),
                    BigDecimal.valueOf(35.742),
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
                BigDecimal.valueOf(24383.78),
                BigDecimal.valueOf(153.371),
                BigDecimal.valueOf(158.9851),
                BigDecimal.valueOf(153.371),
                TransactionType.SWITCH_IN,
                null);
        UserTransactionDTO taxTransaction = new UserTransactionDTO(
                LocalDate.parse("2021-07-19"),
                "*** Stamp Duty ***",
                BigDecimal.valueOf(1.22),
                null,
                null,
                null,
                TransactionType.STAMP_DUTY_TAX,
                null);
        UserTransactionDTO sellUserTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2022-08-01"),
                "*Switch Out - To Nifty 50 Index Fund-DP Growth-BSE - , less STT",
                BigDecimal.valueOf(-5000.0),
                BigDecimal.valueOf(-28.261),
                BigDecimal.valueOf(176.9251),
                BigDecimal.valueOf(125.110),
                TransactionType.SWITCH_OUT,
                null);
        UserTransactionDTO sellTaxTransaction = new UserTransactionDTO(
                LocalDate.parse("2022-08-01"),
                "*** STT Paid ***",
                BigDecimal.valueOf(0.05),
                null,
                null,
                null,
                TransactionType.STT_TAX,
                null);
        UserTransactionDTO userTransactionDTO1 = new UserTransactionDTO(
                LocalDate.parse("2021-01-14"),
                "SIP Purchase-BSE - - INA200005166",
                BigDecimal.valueOf(499.98),
                BigDecimal.valueOf(15.965),
                BigDecimal.valueOf(31.3182),
                BigDecimal.valueOf(15.965),
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
        casDTO.folios().getFirst().schemes().add(getICICIUSBlueChipScheme());
        casDTO.folios()
                .getFirst()
                .schemes()
                .getFirst()
                .transactions()
                .add(new UserTransactionDTO(
                        LocalDate.parse("2021-02-04"),
                        "Switch In - From Multicap Fund - DP - Growth-BSE - - INA200005166",
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(3.221),
                        BigDecimal.valueOf(31.0496),
                        BigDecimal.valueOf(19.186),
                        TransactionType.SWITCH_IN,
                        null));
        return casDTO;
    }

    private static UserSchemeDTO getICICIUSBlueChipScheme() {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        UserTransactionDTO userTransactionDTO = new UserTransactionDTO(
                LocalDate.parse("2020-06-22"),
                "SIP Purchase Appln : 72611 - INA100006898",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(2.796),
                BigDecimal.valueOf(35.76),
                BigDecimal.valueOf(2.796),
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
                BigDecimal.valueOf(2999.85),
                BigDecimal.valueOf(10.359),
                BigDecimal.valueOf(289.6),
                BigDecimal.valueOf(10.359),
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
