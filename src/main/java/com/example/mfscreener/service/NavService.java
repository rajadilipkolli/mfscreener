/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.service;

import com.example.mfscreener.model.FundDetailDTO;
import com.example.mfscreener.model.PortfolioDTO;
import com.example.mfscreener.model.Scheme;
import java.io.IOException;
import java.util.List;

public interface NavService {

    Scheme getNav(Long schemeCode);

    Scheme getNavOnDate(Long schemeCode, String date);

    void fetchSchemeDetails(Long schemeCode);

    List<FundDetailDTO> fetchSchemes(String schemeName);

    List<FundDetailDTO> fetchSchemesByFundName(String fundName);

    String upload() throws IOException;

    PortfolioDTO getPortfolio();

    int updateSynonym(Long schemeId, String schemaName);

    void loadFundDetailsIfNotSet();
}
