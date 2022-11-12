/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class NavResponse {
    private String status;
    private Meta meta;

    @JsonProperty("data")
    private List<NAVData> data = new ArrayList<>();
}
