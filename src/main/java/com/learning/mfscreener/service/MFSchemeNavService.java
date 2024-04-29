package com.learning.mfscreener.service;

import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.repository.MFSchemeNavEntityRepository;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MFSchemeNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MFSchemeNavService.class);

    private final MFSchemeNavEntityRepository mfSchemeNavEntityRepository;
    private final MFSchemeRepository mfSchemeRepository;
    private final ResourceLoader resourceLoader;

    public MFSchemeNavService(
            MFSchemeNavEntityRepository mfSchemeNavEntityRepository,
            MFSchemeRepository mfSchemeRepository,
            ResourceLoader resourceLoader) {
        this.mfSchemeNavEntityRepository = mfSchemeNavEntityRepository;
        this.mfSchemeRepository = mfSchemeRepository;
        this.resourceLoader = resourceLoader;
    }

    public void loadHistoricalNavOn31Jan2018ForExistingSchemes() {

        Resource resource = resourceLoader.getResource("classpath:/nav/31Jan2018Navdata.csv");
        try {
            Path path = resource.getFile().toPath();
            List<String> lines = Files.lines(path).parallel().toList();
            List<MFSchemeNavEntity> mfSchemeNavEntities = lines.stream()
                    .skip(1)
                    .map(csvRow -> {
                        // Split the row
                        String[] fields = csvRow.split(",");

                        // Trim and remove quotes
                        for (int i = 0; i < fields.length; i++) {
                            fields[i] = fields[i].trim().replaceAll("^\"+|\"+$", "");
                        }
                        MFSchemeNavEntity mfSchemeNavEntity = new MFSchemeNavEntity();
                        mfSchemeNavEntity.setNav(Float.valueOf(fields[0]));
                        mfSchemeNavEntity.setNavDate(AppConstants.GRAND_FATHERED_DATE);
                        mfSchemeNavEntity.setMfSchemeEntity(
                                mfSchemeRepository.getReferenceById(Long.valueOf(fields[2].replace("\"\"", ""))));
                        return mfSchemeNavEntity;
                    })
                    .toList();
            List<MFSchemeNavEntity> persistedEntities = mfSchemeNavEntityRepository.saveAll(mfSchemeNavEntities);
            LOGGER.info("Persisted : {} rows", persistedEntities.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    public boolean navLoadedFor31Jan2018ForExistingSchemes() {
        return mfSchemeNavEntityRepository.countByNavDate(AppConstants.GRAND_FATHERED_DATE) >= 5908;
    }

    @Transactional
    public boolean navLoadedForClosedOrMergedSchemes() {
        return mfSchemeNavEntityRepository.countByNavDate(AppConstants.GRAND_FATHERED_DATE) >= 9000;
    }
}
