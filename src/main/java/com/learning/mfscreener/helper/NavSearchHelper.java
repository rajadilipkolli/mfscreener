package com.learning.mfscreener.helper;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.service.HistoricalNavService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.util.StringUtils;

public class NavSearchHelper {

    private static final LocalDate GRAND_FATHERED_DATE = LocalDate.of(2018, 1, 31);

    public static BigDecimal getNav(String isin) {
        MFSchemeRepository mfSchemeRepository = SpringContext.getBean(MFSchemeRepository.class);
        Long schemeCode = mfSchemeRepository.getSchemeIdByISIN(isin).orElseGet(() -> {
            HistoricalNavService historicalNavService = SpringContext.getBean(HistoricalNavService.class);
            String historicalGrandFatheredValue =
                    historicalNavService.getHistoricalGrandFatheredValue(isin, GRAND_FATHERED_DATE);
            return StringUtils.hasText(historicalGrandFatheredValue)
                    ? Long.valueOf(historicalGrandFatheredValue)
                    : null;
        });
        if (schemeCode != null) {
            Optional<MFSchemeEntity> mfSchemeDTO =
                    mfSchemeRepository.findBySchemeIdAndMfSchemeNavEntities_NavDate(schemeCode, GRAND_FATHERED_DATE);
            if (mfSchemeDTO.isPresent()) {
                Float nav = mfSchemeDTO.get().getMfSchemeNavEntities().get(0).getNav();
                return BigDecimal.valueOf(nav);
            }
        }

        return BigDecimal.ZERO;
    }

    private NavSearchHelper() {
        throw new UnsupportedOperationException("Constructor can't be initialized");
    }
}
