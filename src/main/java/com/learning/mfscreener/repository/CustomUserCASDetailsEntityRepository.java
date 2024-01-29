package com.learning.mfscreener.repository;

import com.learning.mfscreener.models.entityviews.UserCASDetailsEntityView;

public interface CustomUserCASDetailsEntityRepository {

    UserCASDetailsEntityView findByInvestorEmailAndName(String email, String name);
}
