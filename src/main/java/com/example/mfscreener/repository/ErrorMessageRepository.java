/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.ErrorMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorMessageRepository extends JpaRepository<ErrorMessage, Long> {}
