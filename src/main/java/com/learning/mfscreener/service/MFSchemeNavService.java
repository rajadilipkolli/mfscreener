package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.exception.FileNotFoundException;
import com.learning.mfscreener.repository.MFSchemeNavEntityRepository;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.AppConstants;
import com.learning.mfscreener.utils.ColumnParsingUtility;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

@Service
@Loggable
@Transactional(readOnly = true)
public class MFSchemeNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MFSchemeNavService.class);

    private final MFSchemeNavEntityRepository mfSchemeNavEntityRepository;
    private final MFSchemeRepository mfSchemeRepository;
    private final ResourceLoader resourceLoader;
    private final TransactionTemplate transactionTemplate;

    public MFSchemeNavService(
            MFSchemeNavEntityRepository mfSchemeNavEntityRepository,
            MFSchemeRepository mfSchemeRepository,
            ResourceLoader resourceLoader,
            TransactionTemplate transactionTemplate) {
        this.mfSchemeNavEntityRepository = mfSchemeNavEntityRepository;
        this.mfSchemeRepository = mfSchemeRepository;
        this.resourceLoader = resourceLoader;
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        this.transactionTemplate = transactionTemplate;
    }

    /** Loads the bundled 31 January 2018 NAV values for schemes already in the database. */
    public void loadHistoricalNavOn31Jan2018ForExistingSchemes() {

        Resource resource = resourceLoader.getResource("classpath:/nav/31Jan2018Navdata.csv");
        try {
            Path path = resource.getFile().toPath();
            List<String> lines = Files.lines(path).parallel().toList();
            if (lines.isEmpty()) return;
            ColumnParsingUtility utility = new ColumnParsingUtility(lines.get(0), ",");
            List<MFSchemeNavEntity> mfSchemeNavEntities = lines.stream()
                    .skip(1)
                    .map(csvRow -> {
                        // Split the row
                        String[] fields = csvRow.split(",");

                        // Trim and remove quotes
                        for (int i = 0; i < fields.length; i++) {
                            fields[i] = fields[i].trim().replaceAll("^\"+|\"+$", "");
                        }
                        String navStr = utility.extractFieldValue(fields, AppConstants.CSV_NAV);
                        String schemeIdStr = utility.extractFieldValue(fields, AppConstants.CSV_SCHEME_ID);

                        MFSchemeNavEntity mfSchemeNavEntity = new MFSchemeNavEntity();
                        mfSchemeNavEntity.setNav(new BigDecimal(navStr));
                        mfSchemeNavEntity.setNavDate(AppConstants.GRAND_FATHERED_DATE);
                        mfSchemeNavEntity.setMfSchemeEntity(
                                mfSchemeRepository.getReferenceById(Long.valueOf(schemeIdStr.replace("\"\"", ""))));
                        return mfSchemeNavEntity;
                    })
                    .toList();
            List<MFSchemeNavEntity> persistedEntities =
                    transactionTemplate.execute(status -> mfSchemeNavEntityRepository.saveAll(mfSchemeNavEntities));
            Assert.notNull(persistedEntities, () -> "persistedEntities cant be null");
            LOGGER.info("Persisted : {} rows", persistedEntities.size());
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("DataIntegrityViolationException occurred ", e);
        }
    }

    public boolean navLoadedFor31Jan2018ForExistingSchemes() {
        return mfSchemeNavEntityRepository.countByNavDate(AppConstants.GRAND_FATHERED_DATE) >= 5908;
    }

    public boolean navLoadedForClosedOrMergedSchemes() {
        return mfSchemeNavEntityRepository.countByNavDate(AppConstants.GRAND_FATHERED_DATE) >= 9000;
    }
}
