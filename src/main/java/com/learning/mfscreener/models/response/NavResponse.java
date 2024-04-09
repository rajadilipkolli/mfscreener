/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.models.response;

import com.learning.mfscreener.models.MetaDTO;
import com.learning.mfscreener.models.NAVDataDTO;
import java.io.Serializable;
import java.util.List;

public record NavResponse(String status, MetaDTO meta, List<NAVDataDTO> data) implements Serializable {}
