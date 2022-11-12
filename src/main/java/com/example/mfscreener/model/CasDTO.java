/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.Data;

@Data
public class CasDTO {

    @JsonProperty("statement_period")
    StatementPeriodDTO statementPeriod;

    @JsonProperty("file_type")
    String fileType;

    @JsonProperty("cas_type")
    String casType;

    @JsonProperty("investor_info")
    InvestorInfoDTO investorInfo;

    @JsonProperty("folios")
    ArrayList<FolioDTO> folios;
}
