package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.models.projection.UserFolioDetailsPanProjection;
import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserFolioDetailsEntityRepository extends JpaRepository<UserFolioDetailsEntity, Long> {
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = "schemeEntities")
    List<UserFolioDetailsProjection> findByPan(String pan);

    @Query(
            """
            select u from UserFolioDetailsEntity u join fetch u.schemeEntities
            where u.userCasDetailsEntity.investorInfoEntity.email = :email and u.userCasDetailsEntity.investorInfoEntity.name = :name
            """)
    @Transactional(readOnly = true)
    List<UserFolioDetailsEntity> findByUserEmailAndName(@Param("email") String email, @Param("name") String name);

    @Transactional(readOnly = true)
    UserFolioDetailsPanProjection findFirstByUserCasDetailsEntity_IdAndPanKyc(Long id, String panKyc);

    @Modifying
    @Transactional
    @Query("update UserFolioDetailsEntity set pan = :pan where panKyc = 'NOT OK' and userCasDetailsEntity.id = :casId")
    void updatePanByCasId(@Param("pan") String pan, @Param("casId") Long casId);
}
