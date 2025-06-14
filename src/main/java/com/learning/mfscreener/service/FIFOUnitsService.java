package com.learning.mfscreener.service;

import com.learning.mfscreener.exception.GainsException;
import com.learning.mfscreener.models.portfolio.Fund;
import com.learning.mfscreener.models.portfolio.FundType;
import com.learning.mfscreener.models.portfolio.TransactionType;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.utils.AppConstants;
import com.learning.mfscreener.utils.FundTypeUtility;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FIFOUnitsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FIFOUnitsService.class);

    private Fund fund;
    private FundType fundType;
    private final Deque<Transaction> transactionsQueue = new ArrayDeque<>();
    private final GainEntryService gainEntryService;

    private BigDecimal totalInvested = BigDecimal.ZERO;
    private BigDecimal currentBalance = BigDecimal.ZERO;
    private List<GainEntryService> recordedGains = new ArrayList<>();

    public FIFOUnitsService(GainEntryService gainEntryService) {
        this.gainEntryService = gainEntryService;
    }

    public FIFOUnitsService init(Fund fund, List<UserTransactionDTO> userTransactionDTOList) {
        this.fund = fund;
        this.fundType = determineFundType(fund.type(), userTransactionDTOList);
        Map<LocalDate, MergedTransaction> mergedTransactions = mergeTransactions(userTransactionDTOList);
        processTransactions(mergedTransactions);
        return this;
    }

    FundType determineFundType(String type, List<UserTransactionDTO> transactions) {
        FundType fundType;
        if (!"EQUITY".equals(type) && !"DEBT".equals(type)) {
            fundType = FundTypeUtility.deriveFundTypeFromTransactions(transactions);
        } else {
            fundType = FundType.valueOf(type);
        }
        return fundType;
    }

    List<UserTransactionDTO> cleanTransactions(List<UserTransactionDTO> userTransactionDTOList) {
        return userTransactionDTOList.stream().filter(x -> x.amount() != null).toList();
    }

    Map<LocalDate, MergedTransaction> mergeTransactions(List<UserTransactionDTO> userTransactionDTOList) {
        Map<LocalDate, MergedTransaction> mergedTransactions = new TreeMap<>();

        cleanTransactions(userTransactionDTOList).stream()
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

    void processTransactions(Map<LocalDate, MergedTransaction> mergedTransactions) {
        List<LocalDate> transactionDates = new ArrayList<>(mergedTransactions.keySet());
        Collections.sort(transactionDates);
        transactionDates.forEach(localDate -> processTransactionDate(mergedTransactions.get(localDate)));
    }

    void processTransactionDate(MergedTransaction mergedTransaction) {
        List<UserTransactionDTO> userTransactionDTOS = mergedTransaction.getTransactions();
        LocalDate dt = mergedTransaction.getDate();
        if (userTransactionDTOS.size() == 2 && dt.isAfter(AppConstants.TAX_STARTED_DATE)) {
            findTaxFromTransactionsAndProcess(userTransactionDTOS, dt);
        } else if (userTransactionDTOS.size() > 2 && dt.isAfter(AppConstants.TAX_STARTED_DATE)) {
            processMultipleTransactions(userTransactionDTOS, dt);
        } else {
            processStandardTransactions(userTransactionDTOS);
        }
    }

    void processMultipleTransactions(List<UserTransactionDTO> userTransactionDTOS, LocalDate dt) {
        int splitIndex = 0;
        boolean found = false;
        for (int i = 0; i < userTransactionDTOS.size(); i++) {
            splitIndex = i;
            if (userTransactionDTOS.get(i).type().compareTo(TransactionType.STT_TAX) == 0
                    || userTransactionDTOS.get(i).type().compareTo(TransactionType.REDEMPTION) == 0) {
                found = true;
                break;
            }
        }

        if (!found) {
            splitIndex = userTransactionDTOS.size();
        }

        List<UserTransactionDTO> buyTransactionList = userTransactionDTOS.subList(0, splitIndex);
        List<UserTransactionDTO> sellTransactionList =
                userTransactionDTOS.subList(splitIndex, userTransactionDTOS.size());

        processesGroupedTransactions(dt, buyTransactionList, false);
        processesGroupedTransactions(dt, sellTransactionList, true);
    }

    void processesGroupedTransactions(
            LocalDate dt, List<UserTransactionDTO> userTransactionDTOList, boolean sellOrder) {
        if (!userTransactionDTOList.isEmpty()) {
            groupTransactions(userTransactionDTOList, sellOrder)
                    .forEach(groupedTransaction -> findTaxFromTransactionsAndProcess(groupedTransaction, dt));
        }
    }

    List<List<UserTransactionDTO>> groupTransactions(List<UserTransactionDTO> transactionDTOList, boolean sellOrder) {
        // If list size is less than or equal to 2, no need to regroup
        if (transactionDTOList.size() <= 2) {
            return Collections.singletonList(transactionDTOList);
        }
        int mid = (transactionDTOList.size() + 1) / 2; // Round up

        List<UserTransactionDTO> firstHalf = transactionDTOList.subList(0, mid);
        List<UserTransactionDTO> secondHalf = transactionDTOList.subList(mid, transactionDTOList.size());
        if (sellOrder) {
            Collections.reverse(secondHalf); // Reverse the second half if the flag is true
        }
        return IntStream.range(0, firstHalf.size())
                .mapToObj(i -> {
                    List<UserTransactionDTO> pair = new ArrayList<>();
                    pair.add(firstHalf.get(i));
                    if (i < secondHalf.size()) {
                        pair.add(secondHalf.get(i));
                    }
                    return pair;
                })
                .toList();
    }

    void processStandardTransactions(List<UserTransactionDTO> userTransactionDTOS) {
        userTransactionDTOS.forEach(userTransactionDTO -> {
            if (userTransactionDTO.units() == null) {
                LOGGER.error("Unhandled dividend Transactions");
            } else {
                processTransaction(userTransactionDTO);
            }
        });
    }

    void processTransaction(UserTransactionDTO userTransactionDTO) {
        if (userTransactionDTO.units() > 0) {
            buy(
                    userTransactionDTO.date(),
                    BigDecimal.valueOf(userTransactionDTO.units()),
                    BigDecimal.valueOf(userTransactionDTO.nav()),
                    BigDecimal.ZERO);
        } else if (userTransactionDTO.units() < 0) {
            sell(
                    userTransactionDTO.date(),
                    BigDecimal.valueOf(userTransactionDTO.units()),
                    BigDecimal.valueOf(userTransactionDTO.nav()),
                    BigDecimal.ZERO);
        }
    }

    void findTaxFromTransactionsAndProcess(List<UserTransactionDTO> userTransactionDTOS, LocalDate dt) {
        // if buy we will have STAMP_DUTY_TAX, for sale we will have STT_TAX
        if (userTransactionDTOS.size() == 2) {
            if (userTransactionDTOS.get(1).type().compareTo(TransactionType.STAMP_DUTY_TAX) == 0) {
                // buy
                Double tax = userTransactionDTOS.get(1).amount();
                buy(
                        dt,
                        BigDecimal.valueOf(userTransactionDTOS.getFirst().units()),
                        BigDecimal.valueOf(userTransactionDTOS.getFirst().nav()),
                        BigDecimal.valueOf(tax));
            } else if (userTransactionDTOS.getFirst().type().compareTo(TransactionType.STT_TAX) == 0) {
                // sell
                Double tax = userTransactionDTOS.getFirst().amount();
                sell(
                        dt,
                        BigDecimal.valueOf(userTransactionDTOS.get(1).units()),
                        BigDecimal.valueOf(userTransactionDTOS.get(1).nav()),
                        BigDecimal.valueOf(tax));
            }
        } else if (userTransactionDTOS.size() == 1) {
            if (userTransactionDTOS.getFirst().type().compareTo(TransactionType.REDEMPTION) == 0
                    || userTransactionDTOS.getFirst().type().compareTo(TransactionType.SWITCH_OUT) == 0) {
                sell(
                        dt,
                        BigDecimal.valueOf(userTransactionDTOS.getFirst().units()),
                        BigDecimal.valueOf(userTransactionDTOS.getFirst().nav()),
                        BigDecimal.ZERO);
            } else if (userTransactionDTOS.getFirst().type().compareTo(TransactionType.PURCHASE) == 0
                    || userTransactionDTOS.getFirst().type().compareTo(TransactionType.SWITCH_IN) == 0) {
                buy(
                        dt,
                        BigDecimal.valueOf(userTransactionDTOS.getFirst().units()),
                        BigDecimal.valueOf(userTransactionDTOS.getFirst().nav()),
                        BigDecimal.ZERO);
            }
        }
    }

    void buy(LocalDate txnDate, BigDecimal quantity, BigDecimal nav, BigDecimal tax) {
        transactionsQueue.add(new Transaction(txnDate, quantity, nav, tax));
        totalInvested = totalInvested.add(quantity.multiply(nav));
        currentBalance = currentBalance.add(quantity);
    }

    void sell(LocalDate sellDate, BigDecimal quantity, BigDecimal nav, BigDecimal tax) throws GainsException {
        String finYear = LocalDateUtility.getFinYear(sellDate);
        BigDecimal originalQuantity = quantity.abs();
        BigDecimal pendingUnits = originalQuantity;

        while (pendingUnits.compareTo(new BigDecimal("0.01")) >= 0) {
            Transaction txn = transactionsQueue.pollFirst();
            if (txn == null) {
                throw new GainsException("FIFOUnits mismatch for fund. Please contact support.");
            }

            LocalDate purchaseDate = txn.txnDate();
            BigDecimal units = txn.units();
            BigDecimal purchaseNav = txn.nav();
            BigDecimal purchaseTax = txn.tax();

            BigDecimal gainUnits = units.min(pendingUnits);

            BigDecimal purchaseValue = gainUnits.multiply(purchaseNav).setScale(2, RoundingMode.HALF_UP);
            BigDecimal saleValue = gainUnits.multiply(nav).setScale(2, RoundingMode.HALF_UP);
            BigDecimal stampDuty = purchaseTax.multiply(gainUnits).divide(units, 2, RoundingMode.HALF_UP);
            BigDecimal stt = tax.multiply(gainUnits).divide(originalQuantity, 2, RoundingMode.HALF_UP);

            GainEntryService ge = gainEntryService.init(
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
            recordedGains.add(ge);

            currentBalance = currentBalance.subtract(gainUnits);
            totalInvested = totalInvested.subtract(purchaseValue);

            pendingUnits = pendingUnits.subtract(units);
            if (pendingUnits.compareTo(BigDecimal.ZERO) < 0 && purchaseNav != null) {
                // Sale is partially matched against the last buy transactions Re-add the remaining units to the FIFO
                // queue
                transactionsQueue.addFirst(
                        new Transaction(purchaseDate, pendingUnits.negate(), purchaseNav, purchaseTax));
            }
        }
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public FIFOUnitsService setTotalInvested(BigDecimal totalInvested) {
        this.totalInvested = totalInvested;
        return this;
    }

    public List<GainEntryService> getRecordedGains() {
        return recordedGains;
    }

    public FIFOUnitsService setRecordedGains(List<GainEntryService> recordedGains) {
        this.recordedGains = recordedGains;
        return this;
    }

    public record Transaction(LocalDate txnDate, BigDecimal units, BigDecimal nav, BigDecimal tax) {}

    static class MergedTransaction {

        private final LocalDate date;
        private final List<UserTransactionDTO> transactions;

        public MergedTransaction(LocalDate date) {
            this.date = date;
            this.transactions = new ArrayList<>();
        }

        public void add(UserTransactionDTO txn) {
            this.transactions.add(txn);
        }

        public LocalDate getDate() {
            return date;
        }

        public List<UserTransactionDTO> getTransactions() {
            return transactions;
        }
    }
}
