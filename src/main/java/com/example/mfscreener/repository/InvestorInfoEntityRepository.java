package com.example.mfscreener.repository;

import com.example.mfscreener.entities.InvestorInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorInfoEntityRepository extends JpaRepository<InvestorInfoEntity, Long> {
    boolean existsByEmailAndName(String email, String name);
}
