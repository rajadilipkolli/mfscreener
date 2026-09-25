package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.repository.InvestorInfoEntityRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Loggable
@Transactional(readOnly = true)
public class InvestorInfoService {

    private final InvestorInfoEntityRepository investorInfoEntityRepository;

    public InvestorInfoService(InvestorInfoEntityRepository investorInfoEntityRepository) {
        this.investorInfoEntityRepository = investorInfoEntityRepository;
    }

    @Cacheable(value = "emailAndName", unless = "#result == false")
    public boolean existsByEmailAndName(String email, String name) {
        return this.investorInfoEntityRepository.existsByEmailAndName(email, name);
    }
}
