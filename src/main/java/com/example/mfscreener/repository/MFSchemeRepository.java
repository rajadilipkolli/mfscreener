/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.model.FundDetailDTO;
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
public interface MFSchemeRepository extends JpaRepository<MFScheme, Long> {

    @Query("select o from MFScheme o JOIN FETCH o.mfSchemeNavies msn where o.schemeId = :schemeId")
    @Transactional(readOnly = true)
    Optional<MFScheme> findBySchemeId(@Param("schemeId") Long aLong);

    @Query(
            "select o from MFScheme o JOIN FETCH o.mfSchemeNavies msn where o.schemeId ="
                    + " :schemeCode and msn.navDate = :date")
    @Transactional(readOnly = true)
    Optional<MFScheme> findBySchemeIdAndNavDate(
            @Param("schemeCode") Long schemeCode, @Param("date") LocalDate navDate);

    @Transactional(readOnly = true)
    @Query("select o.schemeId from MFScheme o where o.fundHouse is null order by o.schemeId desc")
    List<Long> findAllByFundHouseNull();

    @Transactional(readOnly = true)
    @Query("select o.schemeId from MFScheme o")
    List<Long> findAllSchemeIds();

    @Transactional(readOnly = true)
    List<FundDetailDTO> findBySchemeNameIgnoringCaseLike(String schemeName);

    @Transactional(readOnly = true)
    List<FundDetailDTO> findByFundHouseIgnoringCaseLike(String fundHouse);

    @Transactional
    @Modifying
    @Query("update MFScheme o set o.schemeNameAlias =:schemeName where o.schemeId=:schemeId")
    int updateSchemeNameAliasBySchemeId(
            @Param("schemeName") String schemeName, @Param("schemeId") Long schemeId);
}
