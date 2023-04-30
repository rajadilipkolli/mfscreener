package com.example.mfscreener.repository;

import com.example.mfscreener.entities.UserCASDetailsEntity;

public interface CustomCASDetailsEntityRepository {

    UserCASDetailsEntity findByInvestorInfoEntity_EmailAndInvestorInfoEntity_Name(String email, String name);
}
