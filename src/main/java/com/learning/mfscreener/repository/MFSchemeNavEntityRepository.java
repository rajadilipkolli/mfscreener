package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.MFSchemeNavEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MFSchemeNavEntityRepository extends JpaRepository<MFSchemeNavEntity, Long> {

    long countByNavDate(LocalDate navDate);

    @Query(
            """
            select n from MFSchemeNavEntity n
            join fetch n.mfSchemeEntity m
            join fetch m.mfSchemeTypeEntity
            where m.schemeId = :schemeCode and n.navDate = :navDate
            """)
    Optional<MFSchemeNavEntity> findNavBySchemeIdAndDate(
            @Param("schemeCode") Long schemeCode, @Param("navDate") LocalDate navDate);

    @Query("select n.navDate from MFSchemeNavEntity n where n.mfSchemeEntity.schemeId = :schemeCode")
    List<LocalDate> findNavDatesBySchemeId(@Param("schemeCode") Long schemeCode);
}
