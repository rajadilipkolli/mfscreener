package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.MFSchemeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MFSchemeRepository extends JpaRepository<MFSchemeEntity, Long> {

    @Transactional(readOnly = true)
    @Query("select o.schemeId from MFSchemeEntity o")
    List<Long> findAllSchemeIds();
}
