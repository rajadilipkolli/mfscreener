/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.ErrorMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorMessageRepository extends JpaRepository<ErrorMessageEntity, Long> {}
