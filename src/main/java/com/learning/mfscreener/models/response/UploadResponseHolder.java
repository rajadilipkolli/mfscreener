package com.learning.mfscreener.models.response;

import com.learning.mfscreener.entities.UserCASDetailsEntity;
import java.io.Serializable;

public record UploadResponseHolder(UserCASDetailsEntity userCASDetailsEntity, int folioCount, int transactionsCount)
        implements Serializable {}
