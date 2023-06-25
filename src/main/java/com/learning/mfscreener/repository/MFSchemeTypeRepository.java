package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MFSchemeTypeRepository extends JpaRepository<MFSchemeTypeEntity, Integer> {

    @EntityGraph(attributePaths = "mfSchemeEntities")
    Optional<MFSchemeTypeEntity> findBySchemeCategoryAndSchemeType(String schemeCategory, String schemeType);
}
