package com.example.mfscreener.convertor;

import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.entities.MFSchemeNav;
import com.example.mfscreener.model.Scheme;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class NavServiceConvertor implements Converter<Scheme, MFScheme> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Override
    public MFScheme convert(Scheme source) {
        MFScheme mfSchemes = new MFScheme();
        mfSchemes.setSchemeId(Long.valueOf(source.getSchemeCode()));
        mfSchemes.setPayOut(source.getPayout());
        mfSchemes.setSchemeName(source.getSchemeName());
        MFSchemeNav mfSchemenav = new MFSchemeNav();
        mfSchemenav.setNav("N.A.".equals(source.getNav()) ? 0D : Double.parseDouble(source.getNav()));
        mfSchemenav.setNavDate(LocalDate.parse(source.getDate(), DATE_FORMATTER));
        mfSchemes.addSchemeNav(mfSchemenav);
        return mfSchemes;
    }
}
