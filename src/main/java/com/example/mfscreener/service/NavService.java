/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.service;

import com.example.mfscreener.models.MFSchemeDTO;
import com.example.mfscreener.models.projection.FundDetailProjection;
import com.example.mfscreener.models.response.PortfolioResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface NavService {

    MFSchemeDTO getNav(Long schemeCode);

    MFSchemeDTO getNavOnDate(Long schemeCode, String date);

    void fetchSchemeDetails(Long schemeCode);

    List<FundDetailProjection> fetchSchemes(String schemeName);

    List<FundDetailProjection> fetchSchemesByFundName(String fundName);

    PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate date);

    void loadFundDetailsIfNotSet();

    String upload(MultipartFile multipartFile) throws IOException;
}
