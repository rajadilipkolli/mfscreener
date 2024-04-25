package com.learning.mfscreener.service;

import com.learning.mfscreener.exception.GainsException;
import com.learning.mfscreener.models.portfolio.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FIFOUnits {

    private static final Logger LOGGER = LoggerFactory.getLogger(FIFOUnits.class);

    private final Fund fund;
    private final List<UserTransactionDTO> originalTransactions;
    private final FundType fundType;
    private final Map<LocalDate, MergedTransaction> mergedTransactions;
    private final Deque<Transaction> transactions = new ArrayDeque<>();

    private BigDecimal invested = BigDecimal.ZERO;
    private BigDecimal balance = BigDecimal.ZERO;
    private List<GainEntry> gains = new ArrayList<>();

    public FIFOUnits(Fund fund, List<UserTransactionDTO> transactions) throws GainsException {
        this.fund = fund;
        this.originalTransactions = transactions;
        if (!"EQUITY".equals(fund.type()) && !"DEBT".equals(fund.type())) {
            this.fundType = getFundType(transactions);
        } else {
            this.fundType = FundType.valueOf(fund.type());
        }
        this.mergedTransactions = mergeTransactions();

        process();
    }

    public List<UserTransactionDTO> cleanTransactions() {
        return originalTransactions.stream().filter(x -> x.amount() != null).toList();
    }

    public Map<LocalDate, MergedTransaction> mergeTransactions() {
        Map<LocalDate, MergedTransaction> mergedTransactions = new TreeMap<>();

        cleanTransactions().stream()
                .sorted(Comparator.comparing(UserTransactionDTO::date)
                        .thenComparing(UserTransactionDTO::amount, Comparator.reverseOrder()))
                .forEach(txn -> {
                    LocalDate dt = txn.date();
                    if (!mergedTransactions.containsKey(dt)) {
                        mergedTransactions.put(dt, new MergedTransaction(dt));
                    }
                    mergedTransactions.get(dt).add(txn);
                });

        return mergedTransactions;
    }

    public List<GainEntry> process() throws GainsException {
        List<LocalDate> toSort = new ArrayList<>();
        for (LocalDate dt : mergedTransactions.keySet()) {
            toSort.add(dt);
        }
        toSort.sort(null);
        for (LocalDate dt : toSort) {
            MergedTransaction txn = mergedTransactions.get(dt);
            List<UserTransactionDTO> userTransactionDTOS = txn.transactions;
            if (userTransactionDTOS.size() == 2) {
                // if buy we will have STAMP_DUTY_TAX, for sale we will have STT_TAX
                if (userTransactionDTOS.get(1).type().compareTo(TransactionType.STAMP_DUTY_TAX) == 0) {
                    // buy
                    Double tax = userTransactionDTOS.get(1).amount();
                    buy(
                            dt,
                            BigDecimal.valueOf(userTransactionDTOS.get(0).units()),
                            BigDecimal.valueOf(userTransactionDTOS.get(0).nav()),
                            BigDecimal.valueOf(tax));
                } else if (userTransactionDTOS.get(0).type().compareTo(TransactionType.STT_TAX) == 0) {
                    // sell
                    Double tax = userTransactionDTOS.get(0).amount();
                    sell(
                            dt,
                            BigDecimal.valueOf(userTransactionDTOS.get(1).units()),
                            BigDecimal.valueOf(userTransactionDTOS.get(1).nav()),
                            BigDecimal.valueOf(tax));
                }
            } else if (userTransactionDTOS.size() > 2) {
                // TODO handle this.
                LOGGER.error("Unhandled Scenario");
            } else {
                UserTransactionDTO userTransactionDTO = userTransactionDTOS.get(0);
                if (userTransactionDTO.units() == null) {
                    // dividend transactions
                    LOGGER.error("Unhandled dividend Transactions");
                } else {
                    if (userTransactionDTO.units() > 0) {
                        buy(
                                dt,
                                BigDecimal.valueOf(userTransactionDTO.units()),
                                BigDecimal.valueOf(userTransactionDTO.nav()),
                                BigDecimal.ZERO);
                    } else if (userTransactionDTO.units() < 0) {
                        sell(
                                dt,
                                BigDecimal.valueOf(userTransactionDTO.units()),
                                BigDecimal.valueOf(userTransactionDTO.nav()),
                                BigDecimal.ZERO);
                    }
                }
            }
        }
        return gains;
    }

    private void buy(LocalDate txnDate, BigDecimal quantity, BigDecimal nav, BigDecimal tax) {
        transactions.add(new Transaction(txnDate, quantity, nav, tax));
        invested = invested.add(quantity.multiply(nav));
        balance = balance.add(quantity);
    }

    private void sell(LocalDate sellDate, BigDecimal quantity, BigDecimal nav, BigDecimal tax) throws GainsException {
        String finYear = getFinYear(sellDate);
        BigDecimal originalQuantity = quantity.abs();
        BigDecimal pendingUnits = originalQuantity;

        while (pendingUnits.compareTo(new BigDecimal("0.01")) >= 0) {
            Transaction txn = transactions.pollFirst();
            if (txn == null) {
                throw new GainsException("FIFOUnits mismatch for fund. Please contact support.");
            }

            LocalDate purchaseDate = txn.txnDate();
            BigDecimal units = txn.units();
            BigDecimal purchaseNav = txn.nav();
            BigDecimal purchaseTax = txn.tax();

            BigDecimal gainUnits = units.min(pendingUnits);

            BigDecimal purchaseValue = gainUnits.multiply(purchaseNav).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal saleValue = gainUnits.multiply(nav).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal stampDuty = purchaseTax.multiply(gainUnits).divide(units, 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal stt = tax.multiply(gainUnits).divide(originalQuantity, 2, BigDecimal.ROUND_HALF_UP);

            GainEntry ge = new GainEntry(
                    finYear,
                    fund,
                    fundType,
                    purchaseDate,
                    purchaseNav,
                    purchaseValue,
                    stampDuty,
                    sellDate,
                    nav,
                    saleValue,
                    stt,
                    gainUnits);
            gains.add(ge);

            balance = balance.subtract(gainUnits);
            invested = invested.subtract(purchaseValue);

            pendingUnits = pendingUnits.subtract(units);
            if (pendingUnits.compareTo(BigDecimal.ZERO) < 0 && purchaseNav != null) {
                transactions.addFirst(new Transaction(purchaseDate, pendingUnits.negate(), purchaseNav, purchaseTax));
            }
        }
    }

    private String getFinYear(LocalDate sellDate) {
        int year1, year2;
        if (sellDate.getMonthValue() > 3) {
            year1 = sellDate.getYear();
            year2 = sellDate.getYear() + 1;
        } else {
            year1 = sellDate.getYear() - 1;
            year2 = sellDate.getYear();
        }

        if (year1 % 100 != 99) {
            year2 %= 100;
        }

        return String.format("FY%d-%02d", year1, year2);
    }

    /**
     *
     * Detect Fund Type.
     *  - UNKNOWN if there are no redemption transactions
     *  - EQUITY if STT_TAX transactions are present in the portfolio
     *  - DEBT if no STT_TAX transactions are present along with redemptions
     *
     * @param transactions list of transactions for a single fund parsed from the CAS
     * @return type of fund
     */
    private FundType getFundType(List<UserTransactionDTO> transactions) {
        boolean valid = transactions.stream()
                .anyMatch(x -> x.units() != null && x.units() < 0 && x.type() != TransactionType.REVERSAL);

        if (!valid) {
            System.out.println(FundType.UNKNOWN);
            return FundType.UNKNOWN;
        }
        if (transactions.stream().anyMatch(x -> x.type() == TransactionType.STT_TAX)) {
            System.out.println(FundType.EQUITY);
            return FundType.EQUITY;
        } else {
            System.out.println(FundType.DEBT);
            return FundType.DEBT;
        }
    }

    public BigDecimal getInvested() {
        return invested;
    }

    public FIFOUnits setInvested(BigDecimal invested) {
        this.invested = invested;
        return this;
    }

    public List<GainEntry> getGains() {
        return gains;
    }

    public FIFOUnits setGains(List<GainEntry> gains) {
        this.gains = gains;
        return this;
    }

    private static class MergedTransaction {
        private final LocalDate date;
        private final List<UserTransactionDTO> transactions;

        public MergedTransaction(LocalDate date) {
            this.date = date;
            this.transactions = new ArrayList<>();
        }

        public void add(UserTransactionDTO txn) {
            this.transactions.add(txn);
        }
    }

    private record Transaction(LocalDate txnDate, BigDecimal units, BigDecimal nav, BigDecimal tax) {}
}
