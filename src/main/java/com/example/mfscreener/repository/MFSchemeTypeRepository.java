package com.example.mfscreener.repository;

import com.example.mfscreener.entities.MFSchemeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MFSchemeTypeRepository extends JpaRepository<MFSchemeType, Integer> {

    Optional<MFSchemeType> findBySchemeType(String schemeType);

    Optional<MFSchemeType> findBySchemeCategory(String schemeCategory);
}