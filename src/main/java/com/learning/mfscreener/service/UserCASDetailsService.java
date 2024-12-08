package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.models.projection.PortfolioDetailsProjection;
import com.learning.mfscreener.repository.UserCASDetailsEntityRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Service
@Transactional(readOnly = true)
public class UserCASDetailsService {

    private final UserCASDetailsEntityRepository userCASDetailsEntityRepository;

    public UserCASDetailsService(UserCASDetailsEntityRepository userCASDetailsEntityRepository) {
        this.userCASDetailsEntityRepository = userCASDetailsEntityRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserCASDetailsEntity saveEntity(UserCASDetailsEntity casDetailsEntity) {
        return userCASDetailsEntityRepository.save(casDetailsEntity);
    }

    public UserCASDetailsEntity findByInvestorEmailAndName(String email, String name) {
        return userCASDetailsEntityRepository.findByInvestorEmailAndName(email, name);
    }

    public List<PortfolioDetailsProjection> getPortfolioDetailsByPanAndAsOfDate(String panNumber, LocalDate asOfDate) {
        return userCASDetailsEntityRepository.getPortfolioDetails(panNumber, asOfDate);
    }
}
