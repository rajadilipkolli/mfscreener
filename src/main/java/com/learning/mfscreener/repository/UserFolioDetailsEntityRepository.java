package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<UserFolioDetailsEntity> findByUserEmailAndName(@Param("email") String email, @Param("name") String name);
}
