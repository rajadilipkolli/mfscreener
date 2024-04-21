package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.InvestorInfoEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorInfoEntityRepository extends JpaRepository<InvestorInfoEntity, Long> {

    @Cacheable(value = "emailAndName", condition = "#result == true")
    boolean existsByEmailAndName(String email, String name);
}
