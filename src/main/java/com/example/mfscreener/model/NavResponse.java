package com.example.mfscreener.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NavResponse {
    private String status;
    private Meta meta;
    @JsonProperty("data")
    private List<NAVData> data = new ArrayList<>();
}
