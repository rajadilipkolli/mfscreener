/* Licensed under Apache-2.0 2023. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.InvestorInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorInfoEntityRepository extends JpaRepository<InvestorInfoEntity, Long> {
    boolean existsByEmailAndName(String email, String name);
}
