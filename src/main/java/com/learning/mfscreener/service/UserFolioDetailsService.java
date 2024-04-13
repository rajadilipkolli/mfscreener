package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.models.projection.UserFolioDetailsPanProjection;
import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import com.learning.mfscreener.repository.UserFolioDetailsEntityRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Loggable
@Transactional(readOnly = true)
public class UserFolioDetailsService {

    private final UserFolioDetailsEntityRepository userFolioDetailsEntityRepository;

    public UserFolioDetailsService(UserFolioDetailsEntityRepository userFolioDetailsEntityRepository) {
        this.userFolioDetailsEntityRepository = userFolioDetailsEntityRepository;
    }

    public List<UserFolioDetailsProjection> findByPanAndAsOfDate(String pan, LocalDate asOfDate) {
        return userFolioDetailsEntityRepository.findByPanAndAsOfDate(pan, asOfDate);
    }

    public List<UserFolioDetailsEntity> findByUserEmailAndName(String email, String name) {
        return userFolioDetailsEntityRepository.findByUserEmailAndName(email, name);
    }

    public UserFolioDetailsPanProjection findFirstByUserCasIdAndPanKyc(Long userCasID, String ok) {
        return userFolioDetailsEntityRepository.findFirstByUserCasDetailsEntity_IdAndPanKyc(userCasID, ok);
    }

    @Transactional
    public int updatePanByCasId(String pan, Long userCasID) {
        return userFolioDetailsEntityRepository.updatePanByCasId(pan, userCasID);
    }
}
