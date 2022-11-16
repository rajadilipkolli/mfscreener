/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.models.response;

import com.example.mfscreener.models.MetaDTO;
import com.example.mfscreener.models.NAVData;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class NavResponse {
    private String status;
    private MetaDTO meta;

    @JsonProperty("data")
    private List<NAVData> data = new ArrayList<>();
}
