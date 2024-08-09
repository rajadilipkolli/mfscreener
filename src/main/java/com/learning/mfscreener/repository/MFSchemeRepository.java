package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.models.projection.FundDetailProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MFSchemeRepository extends JpaRepository<MFSchemeEntity, Long> {

    Optional<MFSchemeEntity> findByPayOut(String payOut);

    @EntityGraph(attributePaths = {"mfSchemeTypeEntity", "mfSchemeNavEntities"})
    Optional<MFSchemeEntity> findBySchemeIdAndMfSchemeNavEntities_NavDate(
            @Param("schemeCode") Long schemeCode, @Param("date") LocalDate navDate);

    @EntityGraph(attributePaths = {"mfSchemeTypeEntity", "mfSchemeNavEntities"})
    Optional<MFSchemeEntity> findBySchemeId(@Param("schemeId") Long schemeId);

    @Query(
            """
            select new com.learning.mfscreener.models.projection.FundDetailProjection(m.schemeId, m.schemeName) from MFSchemeEntity m
             where UPPER(REPLACE(m.schemeName, '- ', '')) like upper(:schemeName) order by m.schemeId
            """)
    List<FundDetailProjection> findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(@Param("schemeName") String schemeName);

    @Query(
            """
            select new com.learning.mfscreener.models.projection.FundDetailProjection(m.schemeId, m.schemeName) from MFSchemeEntity m
             where upper(m.fundHouse) like upper(:fName) order by m.schemeId
            """)
    List<FundDetailProjection> findByFundHouseLikeIgnoringCaseOrderBySchemeIdAsc(@Param("fName") String fName);

    @Query("select m.schemeId from MFSchemeEntity m where m.payOut = :isin order by m.schemeId")
    List<Long> getSchemeIdByISIN(@Param("isin") String isin);

    @Query("select o.schemeId from MFSchemeEntity o")
    List<Long> findAllSchemeIds();
}
