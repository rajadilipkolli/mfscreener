package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserFolioDetailsEntityRepository extends JpaRepository<UserFolioDetailsEntity, Long> {
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = "schemeEntities")
    List<UserFolioDetailsProjection> findByPan(String pan);
}
