package com.learning.mfscreener.service;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.exception.SchemeNotFoundException;
import com.learning.mfscreener.models.MetaDTO;
import com.learning.mfscreener.models.NAVDataDTO;
import com.learning.mfscreener.models.response.NavResponse;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.repository.MFSchemeTypeRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemeService {

    private final RestTemplate restTemplate;
    private final MFSchemeRepository mfSchemesRepository;
    private final MFSchemeTypeRepository mfSchemeTypeRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;

    void fetchSchemeDetails(Long schemeCode) {
        log.info("Fetching SchemeDetails for AMFISchemeCode :{} ", schemeCode);
        URI uri = UriComponentsBuilder.fromHttpUrl(AppConstants.MFAPI_WEBSITE_BASE_URL + schemeCode)
                .build()
                .toUri();

        ResponseEntity<NavResponse> navResponseResponseEntity =
                this.restTemplate.exchange(uri, HttpMethod.GET, null, NavResponse.class);
        if (navResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
            NavResponse entityBody = navResponseResponseEntity.getBody();
            Assert.notNull(entityBody, () -> "Body Can't be Null");
            MFSchemeEntity mfSchemeEntity = mfSchemesRepository
                    .findBySchemeId(schemeCode)
                    .orElseThrow(
                            () -> new SchemeNotFoundException("Fund with schemeCode " + schemeCode + " Not Found"));
            mergeList(entityBody, mfSchemeEntity);
        }
    }

    void mergeList(NavResponse navResponse, MFSchemeEntity mfSchemeEntity) {
        List<NAVDataDTO> navList = navResponse.getData();

        List<MFSchemeNavEntity> newNavs = navList.stream()
                .map(conversionServiceAdapter::mapNAVDataDTOToMFSchemeNavEntity)
                .filter(nav -> !mfSchemeEntity.getMfSchemeNavEntities().contains(nav))
                .toList();

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
            mfSchemeEntity.setFundHouse(meta.fundHouse());
            mfschemeTypeEntity.addMFScheme(mfSchemeEntity);
            this.mfSchemesRepository.save(mfSchemeEntity);
        }
    }
}
