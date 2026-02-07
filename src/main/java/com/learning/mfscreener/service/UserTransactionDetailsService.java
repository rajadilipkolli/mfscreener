package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import com.learning.mfscreener.repository.UserTransactionDetailsEntityRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Loggable
@Transactional(readOnly = true)
public class UserTransactionDetailsService {

    private final UserTransactionDetailsEntityRepository userTransactionDetailsEntityRepository;

    public UserTransactionDetailsService(
            UserTransactionDetailsEntityRepository userTransactionDetailsEntityRepository) {
        this.userTransactionDetailsEntityRepository = userTransactionDetailsEntityRepository;
    }

    public List<UserTransactionDetailsProjection> fetchTransactions(Long schemeIdInDb, LocalDate asOfDate) {
        // fundId 120502 is dividend only fund... handle balance diligently, need to add Dividends as well to the final
        // balance
        return userTransactionDetailsEntityRepository.getByUserSchemeIdAndTypeNotInAndTransactionDateLessThanEqual(
                schemeIdInDb, asOfDate);
    }

    public List<UserTransactionDetailsEntity> findAllTransactionsByEmailAndName(String email, String name) {
        return userTransactionDetailsEntityRepository.findAllTransactionsByEmailAndName(email, name);
    }
}
