package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.GainsException;
import com.learning.mfscreener.exception.IncompleteCASError;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.Fund;
import com.learning.mfscreener.models.portfolio.GainEntry;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CapitalGainsService {

    private BigDecimal investedAmount = BigDecimal.ZERO;
    private Double currentValue = 0D;
    private final List<GainEntry> gainEntries = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    @Loggable(params = false)
    Map<String, GainEntry> processData(CasDTO casDTO) throws IncompleteCASError {

        for (UserFolioDTO folio : casDTO.folios()) {
            for (UserSchemeDTO scheme : folio.schemes()) {
                List<UserTransactionDTO> transactions = scheme.transactions();
                Fund fund = new Fund(scheme.scheme(), folio.folio(), scheme.isin(), scheme.type());
                if (!transactions.isEmpty()) {
                    if (Double.parseDouble(scheme.myopen()) >= 0.01) {
                        throw new IncompleteCASError(
                                "Incomplete CAS found. For gains computation all folios should have zero opening balance");
                    }
                    try {
                        FIFOUnits fifo = new FIFOUnits(fund, transactions);
                        investedAmount = this.investedAmount.add(fifo.getInvested());
                        currentValue = currentValue + scheme.valuation().value();
                        gainEntries.addAll(fifo.getGains());
                    } catch (GainsException exc) {
                        this.errors.add(fund.scheme() + ", " + exc.getMessage());
                    }
                }
            }
        }
        return gainEntries.stream().collect(Collectors.toMap(GainEntry::finYear, Function.identity()));
    }
}
