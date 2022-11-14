/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.UserCASDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CASDetailsEntityRepository extends JpaRepository<UserCASDetailsEntity, Long> {}
