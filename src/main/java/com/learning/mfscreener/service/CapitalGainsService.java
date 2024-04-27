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
import com.learning.mfscreener.utils.FIFOUnits;
import com.learning.mfscreener.utils.GainEntry;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CapitalGainsService {

    private static final double MIN_OPEN_BALANCE = 0.01;

    private BigDecimal investedAmount = BigDecimal.ZERO;
    private Double currentValue = 0D;
    private final List<GainEntry> gainEntries = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    @Loggable(params = false)
    Map<String, Object> processData(CasDTO casDTO) throws IncompleteCASError {
        casDTO.folios().forEach(this::processFolio);
        return prepareGains(gainEntries);
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
            FIFOUnits fifo = new FIFOUnits(fund, transactions);
            investedAmount = this.investedAmount.add(fifo.getInvested());
            currentValue += scheme.valuation().value();
            gainEntries.addAll(fifo.getGains());
        } catch (GainsException exc) {
            this.errors.add(fund.scheme() + ", " + exc.getMessage());
        }
    }

    Map<String, Object> prepareGains(List<GainEntry> gainEntries) {
        Map<String, Object> summary = new HashMap<>();
        // Sort the gains by fy and fund
        gainEntries.sort(Comparator.comparing(GainEntry::getFinYear).thenComparing(GainEntry::getFundType));
        // Group the gains by fy and fund
        Map<String, List<GainEntry>> groupedGains = new HashMap<>();
        for (GainEntry txn : gainEntries) {
            String key = txn.getFinYear() + "-" + txn.getFundType();
            if (!groupedGains.containsKey(key)) {
                groupedGains.put(key, new ArrayList<>());
            }
            groupedGains.get(key).add(txn);
        }
        for (Map.Entry<String, List<GainEntry>> entry : groupedGains.entrySet()) {
            String[] keys = entry.getKey().split("-");
            String fy = keys[0];
            FundType fund = FundType.valueOf(keys[1]);
            if (!summary.containsKey(fy)) {
                summary.put(fy, new HashMap<String, Object>() {
                    {
                        put("funds", new ArrayList<>());
                        put("total", new HashMap<String, BigDecimal>() {
                            {
                                put("ltcg", BigDecimal.ZERO);
                                put("stcg", BigDecimal.ZERO);
                                put("tax_ltcg", BigDecimal.ZERO);
                            }
                        });
                    }
                });
            }
            Map<String, BigDecimal> netTotal =
                    (Map<String, BigDecimal>) ((Map<String, Object>) summary.get(fy)).get("total");
            Map<String, BigDecimal> total = new HashMap<>() {
                {
                    put("ltcg", BigDecimal.ZERO);
                    put("stcg", BigDecimal.ZERO);
                    put("tax_ltcg", BigDecimal.ZERO);
                }
            };
            String finalFy = fy;
            FundType finalFund = fund;
            Map<String, Object> data = new HashMap<>() {
                {
                    put("fy", finalFy);
                    put("fund", finalFund);
                    put("txns", new ArrayList<Map<String, Object>>());
                }
            };
            for (GainEntry txn : entry.getValue()) {
                total.put("ltcg", total.get("ltcg").add(txn.getLtcg()));
                total.put("stcg", total.get("stcg").add(txn.getStcg()));
                total.put("tax_ltcg", total.get("tax_ltcg").add(txn.getLtcgTaxable()));
                ((List<Map<String, Object>>) data.get("txns")).add(new HashMap<>() {
                    {
                        put("buy_date", txn.getPurchaseDate());
                        put("buy_price", txn.getPurchaseValue());
                        put("units", txn.getUnits());
                        put("coa", txn.getCoa());
                        put("sell_date", txn.getSaleDate());
                        put("sell_price", txn.getSaleValue());
                        put("ltcg", txn.getLtcg());
                        put("stcg", txn.getStcg());
                        put("tax_ltcg", txn.getLtcgTaxable());
                    }
                });
            }
            data.put("total", total);
            ((List<Map<String, Object>>) ((Map<String, Object>) summary.get(fy)).get("funds")).add(data);
            netTotal.put("ltcg", netTotal.get("ltcg").add(total.get("ltcg")));
            netTotal.put("stcg", netTotal.get("stcg").add(total.get("stcg")));
            netTotal.put("tax_ltcg", netTotal.get("tax_ltcg").add(total.get("tax_ltcg")));
            ((Map<String, Object>) summary.get(fy)).put("total", netTotal);
        }
        return summary;
    }
}
