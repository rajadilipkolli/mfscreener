package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.models.projection.FundDetailProjection;
import com.learning.mfscreener.models.projection.SchemeNameAndISIN;
import com.learning.mfscreener.repository.UserSchemeDetailsEntityRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Loggable
@Transactional(readOnly = true)
public class UserSchemeDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSchemeDetailsService.class);

    private final UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository;
    private final SchemeService schemeService;

    public UserSchemeDetailsService(
            UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository, SchemeService schemeService) {
        this.userSchemeDetailsEntityRepository = userSchemeDetailsEntityRepository;
        this.schemeService = schemeService;
    }

    public Optional<SchemeNameAndISIN> findFirstBySchemeCode(Long schemeCode) {
        return userSchemeDetailsEntityRepository.findFirstByAmfi(schemeCode);
    }

    @Transactional
    public UserSchemeDetailsEntity saveUserScheme(UserSchemeDetailsEntity userSchemeDetailsEntity) {
        return userSchemeDetailsEntityRepository.save(userSchemeDetailsEntity);
    }

    public List<UserSchemeDetailsEntity> getSchemesByEmailAndName(String email, String name) {
        return userSchemeDetailsEntityRepository.findByUserEmailAndName(email, name);
    }

    @Loggable
    @Transactional
    public void setAMFIIfNull() {
        List<UserSchemeDetailsEntity> userSchemeDetailsEntities = userSchemeDetailsEntityRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(userSchemeDetailsEntity -> {
            String scheme = userSchemeDetailsEntity.getScheme();
            LOGGER.info("amfi is Null for scheme :{}", scheme);
            // attempting to find ISIN
            if (scheme.contains("ISIN:")) {
                String isin = scheme.substring(scheme.lastIndexOf("ISIN:") + 5).strip();
                if (StringUtils.hasText(isin)) {
                    Optional<MFSchemeEntity> mfSchemeEntity = schemeService.findByPayOut(isin);
                    mfSchemeEntity.ifPresent(schemeEntity -> userSchemeDetailsEntityRepository.updateAmfiAndIsinById(
                            schemeEntity.getSchemeId(), isin, userSchemeDetailsEntity.getId()));
                }
            } else {
                // case where isin and amfi is null
                List<FundDetailProjection> fundDetailProjections = schemeService.fetchSchemes(scheme);
                if (!fundDetailProjections.isEmpty()) {
                    Long schemeId = getSchemeId(fundDetailProjections, scheme);
                    if (null != schemeId) {
                        userSchemeDetailsEntityRepository.updateAmfiAndIsinById(
                                schemeId, null, userSchemeDetailsEntity.getId());
                    }
                }
            }
        });
    }

    Long getSchemeId(List<FundDetailProjection> fundDetailProjections, String scheme) {
        return fundDetailProjections.stream()
                .filter(fundDetailProjection -> (scheme.contains("Income")
                                && fundDetailProjection.schemeName().contains("IDCW"))
                        || (!scheme.contains("Income")
                                && !fundDetailProjection.schemeName().contains("IDCW")))
                .map(FundDetailProjection::schemeId)
                .findFirst()
                .orElse(null);
    }

    @Loggable
    public void loadHistoricalDataIfNotExists() {
        List<Long> historicalDataNotLoadedSchemeIdList =
                userSchemeDetailsEntityRepository.getHistoricalDataNotLoadedSchemeIdList();
        if (!historicalDataNotLoadedSchemeIdList.isEmpty()) {
            List<CompletableFuture<Void>> allSchemesWhereHistoricalDetailsNotLoadedCf =
                    historicalDataNotLoadedSchemeIdList.stream()
                            .map(schemeId ->
                                    CompletableFuture.runAsync(() -> schemeService.fetchSchemeDetails(schemeId)))
                            .toList();
            CompletableFuture.allOf(allSchemesWhereHistoricalDetailsNotLoadedCf.toArray(new CompletableFuture<?>[0]))
                    .join();
            LOGGER.info("Completed loading HistoricalData for schemes that don't exists");
        }
    }
}
