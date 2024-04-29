package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.MFSchemeNavEntity;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MFSchemeNavEntityRepository extends JpaRepository<MFSchemeNavEntity, Long> {

    long countByNavDate(LocalDate navDate);
}
