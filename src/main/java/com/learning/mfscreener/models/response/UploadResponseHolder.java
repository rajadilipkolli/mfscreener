package com.learning.mfscreener.models.response;

import com.learning.mfscreener.entities.UserCASDetailsEntity;

public record UploadResponseHolder(UserCASDetailsEntity userCASDetailsEntity, int folioCount, int transactionsCount) {}
