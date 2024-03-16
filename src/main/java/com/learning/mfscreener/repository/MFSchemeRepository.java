package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.models.projection.FundDetailProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MFSchemeRepository extends JpaRepository<MFSchemeEntity, Long> {

    @Transactional(readOnly = true)
    Optional<MFSchemeEntity> findByPayOut(String payOut);

    @Transactional(readOnly = true)
    @Query("select o.schemeId from MFSchemeEntity o")
    List<Long> findAllSchemeIds();

    @Query(
            """
            select o from MFSchemeEntity o LEFT JOIN FETCH o.mfSchemeNavEntities msn
            where o.schemeId =:schemeCode and msn.navDate =:date
            """)
    @Transactional(readOnly = true)
    Optional<MFSchemeEntity> findBySchemeIdAndMfSchemeNavEntities_NavDate(
            @Param("schemeCode") Long schemeCode, @Param("date") LocalDate navDate);

    @Query(
            """
            select o from MFSchemeEntity o LEFT JOIN FETCH o.mfSchemeNavEntities msn
            where o.schemeId = :schemeId
            """)
    @Transactional(readOnly = true)
    Optional<MFSchemeEntity> findBySchemeId(@Param("schemeId") Long schemeId);

    @Query(
            """
            select new com.learning.mfscreener.models.projection.FundDetailProjection(m.schemeId, m.schemeName) from MFSchemeEntity m
             where upper(m.schemeName) like upper(:schemeName) order by m.schemeId
            """)
    @Transactional(readOnly = true)
    List<FundDetailProjection> findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(@Param("schemeName") String schemeName);

    @Query(
            """
            select new com.learning.mfscreener.models.projection.FundDetailProjection(m.schemeId, m.schemeName) from MFSchemeEntity m
             where upper(m.fundHouse) like upper(:fName) order by m.schemeId
            """)
    @Transactional(readOnly = true)
    List<FundDetailProjection> findByFundHouseLikeIgnoringCaseOrderBySchemeIdAsc(@Param("fName") String fName);
}
