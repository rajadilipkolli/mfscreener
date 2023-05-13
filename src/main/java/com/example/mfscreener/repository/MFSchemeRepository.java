/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.MFSchemeEntity;
import com.example.mfscreener.models.projection.FundDetailProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MFSchemeRepository extends JpaRepository<MFSchemeEntity, Long> {

    @Query("select o from MFSchemeEntity o JOIN FETCH o.mfSchemeNavEntities msn where o.schemeId =" + " :schemeId")
    @Transactional(readOnly = true)
    Optional<MFSchemeEntity> findBySchemeId(@Param("schemeId") Long aLong);

    @Query("select o from MFSchemeEntity o JOIN FETCH o.mfSchemeNavEntities msn where o.schemeId ="
            + " :schemeCode and msn.navDate = :date")
    @Transactional(readOnly = true)
    Optional<MFSchemeEntity> findBySchemeIdAndNavDate(
            @Param("schemeCode") Long schemeCode, @Param("date") LocalDate navDate);

    @Transactional(readOnly = true)
    @Query("select o.schemeId from MFSchemeEntity o where o.fundHouse is null order by o.schemeId" + " desc")
    List<Long> findAllByFundHouseNull();

    @Transactional(readOnly = true)
    @Query("select o.schemeId from MFSchemeEntity o")
    List<Long> findAllSchemeIds();

    @Transactional(readOnly = true)
    List<FundDetailProjection> findBySchemeNameIgnoringCaseLike(String schemeName);

    @Transactional(readOnly = true)
    List<FundDetailProjection> findByFundHouseIgnoringCaseLike(String fundHouse);

    @Transactional
    @Modifying
    @Query("update MFSchemeEntity o set o.schemeNameAlias =:schemeName where o.schemeId=:schemeId")
    int updateSchemeNameAliasBySchemeId(@Param("schemeName") String schemeName, @Param("schemeId") Long schemeId);
}
