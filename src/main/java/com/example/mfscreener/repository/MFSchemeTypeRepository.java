/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.MFSchemeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MFSchemeTypeRepository extends JpaRepository<MFSchemeType, Integer> {

    Optional<MFSchemeType> findBySchemeType(String schemeType);

    @Query(
            "select o from MFSchemeType o JOIN FETCH o.mfSchemes ms where o.schemeCategory ="
                    + " :schemeCategory and o.schemeType = :schemeType")
    @Transactional(readOnly = true)
    Optional<MFSchemeType> findBySchemeCategoryAndSchemeType(
            @Param("schemeCategory") String schemeCategory, @Param("schemeType") String schemeType);
}
