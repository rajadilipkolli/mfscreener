package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MFSchemeTypeRepository extends JpaRepository<MFSchemeTypeEntity, Integer> {

    @Query(
            """
            select o from MFSchemeTypeEntity o JOIN FETCH o.mfSchemeEntities ms
            where o.schemeCategory = :schemeCategory and o.schemeType = :schemeType
            """)
    @Transactional(readOnly = true)
    Optional<MFSchemeTypeEntity> findBySchemeCategoryAndSchemeType(
            @Param("schemeCategory") String schemeCategory, @Param("schemeType") String schemeType);
}