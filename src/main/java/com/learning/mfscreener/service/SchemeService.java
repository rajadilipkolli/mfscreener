package com.learning.mfscreener.service;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.exception.SchemeNotFoundException;
import com.learning.mfscreener.models.MetaDTO;
import com.learning.mfscreener.models.projection.FundDetailProjection;
import com.learning.mfscreener.models.projection.Schemeisin;
import com.learning.mfscreener.models.response.NavResponse;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.repository.MFSchemeTypeRepository;
import com.learning.mfscreener.repository.UserSchemeDetailsEntityRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemeService {

    private final RestClient restClient;
    private final MFSchemeRepository mfSchemeRepository;
    private final MFSchemeTypeRepository mfSchemeTypeRepository;
    private final UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;

    @Loggable
    public void fetchSchemeDetails(Long schemeCode) {
        processResponseEntity(schemeCode, getNavResponseResponseEntity(schemeCode));
    }

    @Loggable
    public void fetchSchemeDetails(String oldSchemeCode, Long newSchemeCode) {
        processResponseEntity(newSchemeCode, getNavResponseResponseEntity(Long.valueOf(oldSchemeCode)));
    }

    void processResponseEntity(Long schemeCode, NavResponse navResponse) {
        Optional<MFSchemeEntity> bySchemeId = mfSchemeRepository.findBySchemeId(schemeCode);
        if (bySchemeId.isEmpty()) {
            // Scenario where scheme is discontinued
            Schemeisin firstByAmfi = userSchemeDetailsEntityRepository
                    .findFirstByAmfi(schemeCode)
                    .orElseThrow(
                            () -> new SchemeNotFoundException("Fund with schemeCode " + schemeCode + " Not Found"));
            String isin = firstByAmfi.getIsin();
            log.error("Found Isin :{}", isin);
        } else {
            mergeList(navResponse, bySchemeId.get(), schemeCode);
        }
    }

    NavResponse getNavResponseResponseEntity(Long schemeCode) {
        return this.restClient.get().uri(getUri(schemeCode)).retrieve().body(NavResponse.class);
    }

    URI getUri(Long schemeCode) {
        log.info("Fetching SchemeDetails for AMFISchemeCode :{} ", schemeCode);
        return UriComponentsBuilder.fromHttpUrl(AppConstants.MFAPI_WEBSITE_BASE_URL + schemeCode)
                .build()
                .toUri();
    }

    void mergeList(NavResponse navResponse, MFSchemeEntity mfSchemeEntity, Long schemeCode) {
        if (navResponse.getData().size()
                != mfSchemeEntity.getMfSchemeNavEntities().size()) {
            List<MFSchemeNavEntity> navList = navResponse.getData().stream()
                    .map(navDataDTO -> navDataDTO.withSchemeId(schemeCode))
                    .map(conversionServiceAdapter::mapNAVDataDTOToMFSchemeNavEntity)
                    .toList();
            log.info("No of entries from Server :{} for schemeCode/amfi :{}", navList.size(), schemeCode);
            List<MFSchemeNavEntity> newNavs = navList.stream()
                    .filter(nav -> !mfSchemeEntity.getMfSchemeNavEntities().contains(nav))
                    .toList();

            log.info("No of entities to insert :{} for schemeCode/amfi :{}", newNavs.size(), schemeCode);

            if (!newNavs.isEmpty()) {
                for (MFSchemeNavEntity newSchemeNav : newNavs) {
                    mfSchemeEntity.addSchemeNav(newSchemeNav);
                }
                final MetaDTO meta = navResponse.getMeta();
                MFSchemeTypeEntity mfschemeTypeEntity = this.mfSchemeTypeRepository
                        .findBySchemeCategoryAndSchemeType(meta.schemeCategory(), meta.schemeType())
                        .orElseGet(() -> {
                            MFSchemeTypeEntity entity = new MFSchemeTypeEntity();
                            entity.setSchemeType(meta.schemeType());
                            entity.setSchemeCategory(meta.schemeCategory());
                            return this.mfSchemeTypeRepository.save(entity);
                        });
                // As fund house is set at initializing, removing from here
                //                mfSchemeEntity.setFundHouse(meta.fundHouse());
                mfschemeTypeEntity.addMFScheme(mfSchemeEntity);
                try {
                    this.mfSchemeRepository.save(mfSchemeEntity);
                } catch (ConstraintViolationException | DataIntegrityViolationException exception) {
                    log.error("ConstraintViolationException or DataIntegrityViolationException ", exception);
                }
            }
        } else {
            log.info("data in db and from service is same hence ignoring");
        }
    }

    @Loggable
    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        String sName = "%" + schemeName.toUpperCase(Locale.ROOT) + "%";
        log.info("Fetching schemes with :{}", sName);
        return this.mfSchemeRepository.findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(sName);
    }

    @Loggable
    public List<FundDetailProjection> fetchSchemesByFundName(String fundName) {
        String fName = "%" + fundName.toUpperCase(Locale.ROOT) + "%";
        log.info("Fetching schemes available for fundHouse :{}", fName);
        return this.mfSchemeRepository.findByFundHouseLikeIgnoringCaseOrderBySchemeIdAsc(fName);
    }

    public void setAMFIIfNull() {
        List<UserSchemeDetailsEntity> userSchemeDetailsEntities = userSchemeDetailsEntityRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(userSchemeDetailsEntity -> {
            String scheme = userSchemeDetailsEntity.getScheme();
            log.info("amfi is Null for scheme :{}", scheme);
            // attempting to find ISIN
            if (scheme.indexOf("ISIN:") != 0) {
                String isin = scheme.substring(scheme.lastIndexOf("ISIN:") + 5).strip();
                if (StringUtils.hasText(isin)) {
                    Optional<MFSchemeEntity> mfSchemeEntity = mfSchemeRepository.findByPayOut(isin);
                    mfSchemeEntity.ifPresent(schemeEntity -> userSchemeDetailsEntityRepository.updateAmfiAndIsinById(
                            schemeEntity.getSchemeId(), isin, userSchemeDetailsEntity.getId()));
                }
            }
        });
    }

    public List<UserSchemeDetailsEntity> getSchemesByEmailAndName(String email, String name) {
        return this.userSchemeDetailsEntityRepository.findByUserEmailAndName(email, name);
    }
}
