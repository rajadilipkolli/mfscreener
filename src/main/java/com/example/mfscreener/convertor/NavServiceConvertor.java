package com.example.mfscreener.convertor;

import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.entities.MFSchemeNav;
import com.example.mfscreener.model.Scheme;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NavServiceConvertor implements Converter<Scheme, MFScheme> {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Override
    public MFScheme convert(Scheme source) {
        MFScheme mfSchemes = new MFScheme();
        mfSchemes.setSchemeId(Long.valueOf(source.schemeCode()));
        mfSchemes.setPayOut(source.payout());
        mfSchemes.setSchemeName(source.schemeName());
        MFSchemeNav mfSchemenav = new MFSchemeNav();
        mfSchemenav.setNav("N.A.".equals(source.nav()) ? 0D : Double.parseDouble(source.nav()));
        mfSchemenav.setNavDate(LocalDate.parse(source.date(), DATE_FORMATTER));
        mfSchemes.addSchemeNav(mfSchemenav);
        return mfSchemes;
    }
}
