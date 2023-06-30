package com.learning.mfscreener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.PortfolioDetailsDTO;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.response.PortfolioResponse;
import com.learning.mfscreener.repository.UserCASDetailsEntityRepository;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final ObjectMapper objectMapper;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final UserCASDetailsEntityRepository casDetailsEntityRepository;
    private final NavService navService;

    public String upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = this.objectMapper.readValue(multipartFile.getBytes(), CasDTO.class);
        UserCASDetailsEntity casDetailsEntity;
        casDetailsEntity = this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
        var persistedCasDetailsEntity = this.casDetailsEntityRepository.save(casDetailsEntity);

        return "Uploaded with id " + persistedCasDetailsEntity.getId();
    }

    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate) {
        if (asOfDate.isAfter(LocalDate.now())) {
            asOfDate = LocalDate.now().minusDays(1);
        }

        LocalDate finalAsOfDate = asOfDate;
        List<PortfolioDetailsDTO> portfolioDetailsDTOS =
                casDetailsEntityRepository.getPortfolioDetails(panNumber, asOfDate).stream()
                        .filter(portfolioDetailsProjection -> portfolioDetailsProjection.getSchemeId() != null)
                        .map(portfolioDetails -> {
                            MFSchemeDTO scheme = navService.getNavByDateWithRetry(
                                    portfolioDetails.getSchemeId(), LocalDateUtility.getAdjustedDate(finalAsOfDate));
                            float totalValue = portfolioDetails.getBalanceUnits() * Float.parseFloat(scheme.nav());

                            return new PortfolioDetailsDTO(
                                    totalValue,
                                    portfolioDetails.getSchemeName(),
                                    portfolioDetails.getFolioNumber(),
                                    scheme.date());
                        })
                        .toList();

        //        List<PortfolioDetailsDTO> portfolioDetailsDTOS =
        //                completableFutureList.stream().map(CompletableFuture::join).toList();

        return new PortfolioResponse(
                portfolioDetailsDTOS.stream()
                        .map(PortfolioDetailsDTO::totalValue)
                        .reduce(0f, Float::sum),
                portfolioDetailsDTOS);
    }
}
