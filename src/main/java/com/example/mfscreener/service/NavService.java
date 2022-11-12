/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.service;

import com.example.mfscreener.models.PortfolioDTO;
import com.example.mfscreener.models.Scheme;
import com.example.mfscreener.models.projection.FundDetailProjection;
import java.util.List;

public interface NavService {

    Scheme getNav(Long schemeCode);

    Scheme getNavOnDate(Long schemeCode, String date);

    void fetchSchemeDetails(Long schemeCode);

    List<FundDetailProjection> fetchSchemes(String schemeName);

    List<FundDetailProjection> fetchSchemesByFundName(String fundName);

    PortfolioDTO getPortfolio();

    void loadFundDetailsIfNotSet();
}
