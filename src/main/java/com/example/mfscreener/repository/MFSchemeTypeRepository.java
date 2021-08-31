package com.example.mfscreener.repository;

import com.example.mfscreener.entities.MFSchemeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MFSchemeTypeRepository extends JpaRepository<MFSchemeType, Integer> {

    Optional<MFSchemeType> findBySchemeType(String schemeType);

    @Query("select o from MFSchemeType o JOIN FETCH o.mfSchemes ms where o.schemeCategory = :schemeCategory")
    @Transactional(readOnly = true)
    Optional<MFSchemeType> findBySchemeCategory(@Param("schemeCategory") String schemeCategory);
}