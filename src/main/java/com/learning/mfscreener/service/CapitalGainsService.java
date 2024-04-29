package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.GainsException;
import com.learning.mfscreener.exception.IncompleteCASError;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.Fund;
import com.learning.mfscreener.models.portfolio.FundType;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.response.ProcessCasResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CapitalGainsService {

    private static final double MIN_OPEN_BALANCE = 0.01;
    private static final String TAX_LTCG = "tax_ltcg";
    private static final String LTCG = "ltcg";
    private static final String STCG = "stcg";
    private static final String TOTAL = "total";

    private BigDecimal investedAmount = BigDecimal.ZERO;
    private Double currentValue = 0D;
    private final List<GainEntryService> gainEntries = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private final FIFOUnitsService fifoUnitsService;

    public CapitalGainsService(FIFOUnitsService fifoUnitsService) {
        this.fifoUnitsService = fifoUnitsService;
    }

    @Loggable(params = false)
    ProcessCasResponse processData(CasDTO casDTO) throws IncompleteCASError {
        casDTO.folios().forEach(this::processFolio);
        Map<String, Map<String, Object>> summaryByFY = prepareGains(gainEntries);
        return new ProcessCasResponse(summaryByFY, investedAmount, currentValue, errors, null);
    }

    void processFolio(UserFolioDTO folio) {
        folio.schemes().forEach(scheme -> processScheme(folio, scheme));
    }

    void processScheme(UserFolioDTO folio, UserSchemeDTO scheme) {
        List<UserTransactionDTO> transactions = scheme.transactions();
        Fund fund = new Fund(scheme.scheme(), folio.folio(), scheme.isin(), scheme.type());
        if (!transactions.isEmpty()) {
            validateOpenBalance(scheme);
            processTransactions(fund, transactions, scheme);
        }
    }

    void validateOpenBalance(UserSchemeDTO scheme) {
        if (Double.parseDouble(scheme.myopen()) >= MIN_OPEN_BALANCE) {
            throw new IncompleteCASError(
                    "Incomplete CAS found. For gains computation all folios should have zero opening balance");
        }
    }

    void processTransactions(Fund fund, List<UserTransactionDTO> transactions, UserSchemeDTO scheme) {
        try {
            FIFOUnitsService fifo = fifoUnitsService.init(fund, transactions);
            investedAmount = this.investedAmount.add(fifo.getTotalInvested());
            currentValue += scheme.valuation().value();
            gainEntries.addAll(fifo.getRecordedGains());
        } catch (GainsException exc) {
            this.errors.add(fund.scheme() + ", " + exc.getMessage());
        }
    }

    Map<String, Map<String, Object>> prepareGains(List<GainEntryService> gainEntries) {

        gainEntries.sort(
                Comparator.comparing(GainEntryService::getFinYear).thenComparing(GainEntryService::getFundType));

        Map<String, List<GainEntryService>> groupedGains =
                gainEntries.stream().collect(Collectors.groupingBy(txn -> txn.getFinYear() + "#" + txn.getFundType()));

        Map<String, Map<String, Object>> summary = new HashMap<>();

        groupedGains.forEach((key, value) -> {
            String[] keys = key.split("#");
            String fy = keys[0];
            FundType fund = FundType.valueOf(keys[1]);

            Map<String, Object> fySummary = summary.get(fy);
            if (fySummary == null) {
                fySummary = new HashMap<>();
                fySummary.put("funds", new ArrayList<>());
                Map<String, BigDecimal> totalMap = new HashMap<>();
                totalMap.put(LTCG, BigDecimal.ZERO);
                totalMap.put(STCG, BigDecimal.ZERO);
                totalMap.put(TAX_LTCG, BigDecimal.ZERO);
                fySummary.put(TOTAL, totalMap);
                summary.put(fy, fySummary);
            }

            Map<String, BigDecimal> netTotal = (Map<String, BigDecimal>) fySummary.get(TOTAL);
            Map<String, BigDecimal> total = new HashMap<>();
            total.put(LTCG, BigDecimal.ZERO);
            total.put(STCG, BigDecimal.ZERO);
            total.put(TAX_LTCG, BigDecimal.ZERO);

            Map<String, Object> data = new HashMap<>();
            data.put("fy", fy);
            data.put("fund", fund);
            data.put("txns", new ArrayList<Map<String, Object>>());

            value.forEach(txn -> {
                total.computeIfPresent(LTCG, (k, v) -> v.add(txn.getLtcg()));
                total.computeIfPresent(STCG, (k, v) -> v.add(txn.getStcg()));
                total.computeIfPresent(TAX_LTCG, (k, v) -> v.add(txn.getLtcgTaxable()));

                Map<String, Object> txnData = new HashMap<>();
                txnData.put("buy_date", txn.getPurchaseDate());
                txnData.put("buy_price", txn.getPurchaseValue());
                txnData.put("units", txn.getUnits());
                txnData.put("coa", txn.getCoa());
                txnData.put("sell_date", txn.getSaleDate());
                txnData.put("sell_price", txn.getSaleValue());
                txnData.put(LTCG, txn.getLtcg());
                txnData.put(STCG, txn.getStcg());
                txnData.put(TAX_LTCG, txn.getLtcgTaxable());
                ((List<Map<String, Object>>) data.get("txns")).add(txnData);
            });

            data.put(TOTAL, total);
            ((List<Map<String, Object>>) fySummary.get("funds")).add(data);

            netTotal.computeIfPresent(LTCG, (k, v) -> v.add(total.get(LTCG)));
            netTotal.computeIfPresent(STCG, (k, v) -> v.add(total.get(STCG)));
            netTotal.computeIfPresent(TAX_LTCG, (k, v) -> v.add(total.get(TAX_LTCG)));

            fySummary.put(TOTAL, netTotal);
        });

        return summary;
    }
}
