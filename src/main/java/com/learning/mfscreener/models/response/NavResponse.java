/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.learning.mfscreener.models.MetaDTO;
import com.learning.mfscreener.models.NAVDataDTO;
import java.util.ArrayList;
import java.util.List;

public class NavResponse {
    private String status;
    private MetaDTO meta;

    @JsonProperty("data")
    private List<NAVDataDTO> data = new ArrayList<>();
}
