package com.learning.mfscreener.helper;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.repository.MFSchemeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class NavSearchHelper {

    private static final LocalDate GRAND_FATHERTED_DATE = LocalDate.of(2018, 1, 31);

    public static BigDecimal getNav(String isin) {
        MFSchemeRepository mfSchemeRepository = SpringContext.getBean(MFSchemeRepository.class);
        Optional<Long> schemeIdByISIN = mfSchemeRepository.getSchemeIdByISIN(isin);
        if (schemeIdByISIN.isPresent()) {
            Optional<MFSchemeEntity> mfSchemeDTO = mfSchemeRepository.findBySchemeIdAndMfSchemeNavEntities_NavDate(
                    schemeIdByISIN.get(), GRAND_FATHERTED_DATE);
            if (mfSchemeDTO.isPresent()) {
                Float nav = mfSchemeDTO.get().getMfSchemeNavEntities().get(0).getNav();
                return BigDecimal.valueOf(nav);
            }
        }
        return BigDecimal.ZERO;
    }
}
